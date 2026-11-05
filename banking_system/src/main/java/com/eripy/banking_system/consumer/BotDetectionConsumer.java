package com.eripy.banking_system.consumer;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.security.BlackList;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.EmailService;
import com.eripy.banking_system.service.account.SecurityService;

@Component 
public class BotDetectionConsumer {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BlackListRepository blackListRepository;
    private final EmailService emailService;
    private final SecurityService securityService;
    private final GetterService getterService;
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionConsumer.class);

    public BotDetectionConsumer(
        TransactionRepository transactionRepository,
        AccountRepository accountRepository,
        BlackListRepository blackListRepository,
        EmailService emailService,
        SecurityService securityService,
        GetterService getterService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.blackListRepository = blackListRepository;
        this.emailService = emailService;
        this.securityService = securityService;
        this.getterService = getterService;
    }

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "smurffing")
    public void analyzingSmurffing(UserBehaviorAnalyzedEvent event) {
        Account account = getterService.getAccountFromDatabase(event.id());
        if (account.getFixedDepositAccount() != null) return;
        logger.info("Evaluating transactions from the last minutes");
        List<Transaction> transactionsMinute2 = transactionRepository
            .findRecentTransactions(
                account, 
                LocalDateTime.now().minusMinutes(2)
            );
        List<Transaction> transactionsMinute1 = transactionRepository
            .findRecentTransactions(
                account, 
                LocalDateTime.now().minusMinutes(1)
            );
        if (transactionsMinute2.size() >= 7 || transactionsMinute1.size() >= 5) {
            logger.info("Warning: Suspicious Bot Activity Detected");
            logger.info("Add the banned user to Redis");
            securityService.saveInRedis(account);
            logger.info("Add the account to black list");
            account.setAccountStatus(AccountStatus.closed);
            accountRepository.save(account);
            blackListRepository.save(new BlackList(account.getUser())); 
            emailService.penalizationEmail(account, null, "ban", "use of bots");
        }  
        logger.info("The user passed the bot test"); 
    } 
}
