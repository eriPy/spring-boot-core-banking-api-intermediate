package com.eripy.banking_system.consumer;

import java.time.Duration;
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
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.penalties.AccountPenaltiesByFlaggedRepository;
import com.eripy.banking_system.repositories.penalties.AccountPenaltiesBySmurfingRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.penalties.HistoryRepository;
import com.eripy.banking_system.repositories.penalties.OperationTokenRepository;
import com.eripy.banking_system.service.GetterService;

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
    private static final Logger logger = LoggerFactory.getLogger(SmurfingAbuseDetectionConsumer.class);

    public SmurfingAbuseDetectionConsumer(
        OperationTokenRepository operationTokenRepository,
        RedisTemplate<String, String> redis,
        HistoryRepository historyRepository,
        AccountPenaltiesBySmurfingRepository accountPenaltiesBySmurfingRepository,
        AccountPenaltiesByFlaggedRepository accountPenaltiesByFlaggedRepository,
        AccountRepository accountRepository,
        BlackListRepository blackListRepository,
        GetterService getterService
    ) {
        this.operationTokenRepository = operationTokenRepository;
        this.redis = redis;
        this.historyRepository = historyRepository;
        this.accountPenaltiesBySmurfingRepository = accountPenaltiesBySmurfingRepository;
        this.accountPenaltiesByFlaggedRepository =  accountPenaltiesByFlaggedRepository;
        this.accountRepository = accountRepository;
        this.blackListRepository = blackListRepository;
        this.getterService = getterService;
    }

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "tokens")
    public void analyzingTokens(UserBehaviorAnalyzedEvent event) {
        logger.info("Evaluating tokens from the last 15 minutes");
        List<OperationToken> tokens = operationTokenRepository.
            findByAccountAndCreatedAtBeforeOrderByCreatedAtDesc(
                event.account(), 
                LocalDateTime.now().minusMinutes(15)
            );
        if (tokens.size() == 5) {
            logger.info("Evaluating possible fraud");
            logger.info("Evaluating if user has a history");
            if (historyRepository.existsByAccount(event.account())) {
                History history = historyRepository.findByAccount(event.account());
                if (accountPenaltiesByFlaggedRepository.existsByHistory(history)) {
                    banAccount(event.account(), history, null);
                    return;
                }
            }
            logger.info("User not found in Flagged table, evaluating possible smurfing");
            String smurfing = redis.opsForValue().get("penalized-account-" + event.account().getId());
            if (smurfing == null) {
                logger.info("User not found in redis in process to penalized for smurfing");
            }
            logger.info("User found penalized for smurfing");
            History history = historyRepository.findByAccount(event.account());
            AccountPenaltiesBySmurfing aps = accountPenaltiesBySmurfingRepository.findByHistory(history);
            LocalDateTime bannedUntil = evaluateTime(aps);
            if (bannedUntil.isBefore(LocalDateTime.now().plusDays(30))) {
                PenalizedAccount pa = null;
            } else {
                banAccount(event.account(), history, aps);
            }
            
        }
        logger.info("The user passed the smurfing test");
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
        blackListRepository.save(new BlackList(account));
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
        redis.opsForValue().set(
            "banned-account-" + account.getId(), 
            "Sanctioned for bots", 
            Duration.ofMinutes(15)
        );
    }
}
