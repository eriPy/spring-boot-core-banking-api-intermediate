package com.eripy.banking_system.consumer;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.security.AccountPenaltiesBySmurfing;
import com.eripy.banking_system.entities.security.BlackList;
import com.eripy.banking_system.entities.security.History;
import com.eripy.banking_system.entities.security.OperationToken;
import com.eripy.banking_system.entities.security.PenalizedAccount;
import com.eripy.banking_system.entities.security.PenalizedReason;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.penalties.AccountPenaltiesByFlaggedRepository;
import com.eripy.banking_system.repositories.penalties.AccountPenaltiesBySmurfingRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.penalties.HistoryRepository;
import com.eripy.banking_system.repositories.penalties.OperationTokenRepository;
import com.eripy.banking_system.repositories.penalties.PenalizedAccountRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.EmailService;
import com.eripy.banking_system.service.account.SecurityService;

@Component 
public class SmurfingAbuseDetectionConsumer {
    private final OperationTokenRepository operationTokenRepository;
    private final RedisTemplate<String, String> redis;
    private final HistoryRepository historyRepository;
    private final AccountPenaltiesByFlaggedRepository accountPenaltiesByFlaggedRepository;
    private final AccountPenaltiesBySmurfingRepository accountPenaltiesBySmurfingRepository;
    private final AccountRepository accountRepository;
    private final BlackListRepository blackListRepository;
    private final GetterService getterService;
    private final PenalizedAccountRepository penalizedAccountRepository;
    private final EmailService emailService;
    private final SecurityService securityService;
    private static final Logger logger = LoggerFactory.getLogger(SmurfingAbuseDetectionConsumer.class);

    public SmurfingAbuseDetectionConsumer(
        OperationTokenRepository operationTokenRepository,
        RedisTemplate<String, String> redis,
        HistoryRepository historyRepository,
        AccountPenaltiesBySmurfingRepository accountPenaltiesBySmurfingRepository,
        AccountPenaltiesByFlaggedRepository accountPenaltiesByFlaggedRepository,
        AccountRepository accountRepository,
        BlackListRepository blackListRepository,
        GetterService getterService,
        PenalizedAccountRepository penalizedAccountRepository,
        EmailService emailService,
        SecurityService securityService
    ) {
        this.operationTokenRepository = operationTokenRepository;
        this.redis = redis;
        this.historyRepository = historyRepository;
        this.accountPenaltiesBySmurfingRepository = accountPenaltiesBySmurfingRepository;
        this.accountPenaltiesByFlaggedRepository =  accountPenaltiesByFlaggedRepository;
        this.accountRepository = accountRepository;
        this.blackListRepository = blackListRepository;
        this.getterService = getterService;
        this.penalizedAccountRepository = penalizedAccountRepository;
        this.emailService = emailService;
        this.securityService = securityService;
    }

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "tokens")
    public void analyzingTokens(UserBehaviorAnalyzedEvent event) {
        Account account = getterService.getAccountFromDatabase(event.id());
        logger.info("Evaluating tokens from the last 15 minutes");
        List<OperationToken> tokens = operationTokenRepository.
            findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(
                account, 
                LocalDateTime.now().minusMinutes(15)
            );
        if (tokens.size() >= 8) {
            logger.info("Evaluating possible fraud");
            PenalizedReason pr = getterService.getPenalizedReason("smurfing");
            logger.info("Evaluating if user has a history");
            History history = historyRepository.findByAccount(account)
                .orElseGet(() -> {
                    logger.info("The user has no history");
                    return new History(account);
                });
            if (history.getId() == null) historyRepository.save(history);
            if (accountPenaltiesByFlaggedRepository.existsByHistory(history)) {
                banAccount(account, history, null);
                return;
            }
            logger.info("User not found in Flagged table, evaluating possible smurfing");
            String smurfing = redis.opsForValue().get("penalized-account-" + account.getId());
            if (smurfing == null) {
                logger.info("User not found in redis in process to penalized for smurfing");
                AccountPenaltiesBySmurfing aps = accountPenaltiesBySmurfingRepository.
                    existsByHistory(history) ?
                    accountPenaltiesBySmurfingRepository.findByHistory(history) :
                    new AccountPenaltiesBySmurfing(history, 0);
                aps.setWarnings(aps.getWarnings() + 1);
                if (aps.getId() != null && aps.getWarnings() % 3 == 0) { 
                    frozenAccount(aps, account, pr);
                    return;
                }
                accountPenaltiesBySmurfingRepository.save(aps);
                securityService.saveInRedis(account);
                return;
            }
            logger.info("User found penalized for smurfing");
            AccountPenaltiesBySmurfing aps = accountPenaltiesBySmurfingRepository.findByHistory(history);
            aps.setWarnings(aps.getWarnings() + 1);
            if(!frozenAccount(aps, account, pr)) banAccount(account, history, aps);
        }
        logger.info("The user passed the smurfing test");
    }

    private boolean frozenAccount(AccountPenaltiesBySmurfing aps, Account account, PenalizedReason pr) {
        LocalDateTime bannedUntil = evaluateTime(aps);
        if (bannedUntil.isBefore(LocalDateTime.now().plusDays(30))) {
            logger.info("Saving the account frozing on database");
            account.setAccountStatus(AccountStatus.frozen);
            accountRepository.save(account);
            PenalizedAccount pa = new PenalizedAccount(account, bannedUntil, pr);
            penalizedAccountRepository.save(pa);
            logger.info("Saving the penalized information");
            aps.setSuspensions(aps.getSuspensions() + 1);
            accountPenaltiesBySmurfingRepository.save(aps);
            securityService.saveInRedis(account);
            emailService.penalizationEmail(
                account, 
                bannedUntil, 
                "frozen", 
                "unauthorized account manipulation");
            return true;
        }
        return false;
    }

    private LocalDateTime evaluateTime(AccountPenaltiesBySmurfing aps) {
        switch (aps.getSuspensions()) {
            case 2:
                return LocalDateTime.now().plusYears(1);
            case 1:
                return LocalDateTime.now().plusDays(7);
            case 0:
                return LocalDateTime.now().plusDays(1);
            default:
                throw new InconsistenceDataException("Something went wrong");
            }
    }

    private void banAccount(Account account, History history, AccountPenaltiesBySmurfing aps) {
        account.setAccountStatus(AccountStatus.closed);
        accountRepository.save(account);
        logger.info("Add user to black list");
        blackListRepository.save(new BlackList(account.getUser()));
        logger.info("Add smurfing information");
        boolean apsExists = accountPenaltiesBySmurfingRepository.existsByHistory(history);
        if (aps != null || apsExists) {
            if (aps == null) aps = accountPenaltiesBySmurfingRepository.findByHistory(history);
            aps.setSuspensions(aps.getSuspensions() + 1);
        } else {
            aps = new AccountPenaltiesBySmurfing(history, 0, 1);
        }
        accountPenaltiesBySmurfingRepository.save(aps);
        logger.info(apsExists ? "Update on penalties by smurfing tabe" : "Account saved on penalties by smurfing table");
        logger.info("Add the banned user to Redis");
        securityService.saveInRedis(account);
        emailService.penalizationEmail(
            account, 
            null, 
            "ban", 
        "unusual transaction activity"
    );
    }
}
