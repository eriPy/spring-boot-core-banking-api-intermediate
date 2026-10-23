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
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.security.BlackList;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.account.EmailService;

@Component 
public class BotDetectionConsumer {
    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;
    private final BlackListRepository blackListRepository;
    private final RedisTemplate<String, String> redis;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(BotDetectionConsumer.class);

    public BotDetectionConsumer(
        TransactionRepository transactionRepository,
        AccountRepository accountRepository,
        BlackListRepository blackListRepository,
        RedisTemplate<String, String> redis,
        EmailService emailService
    ) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
        this.blackListRepository = blackListRepository;
        this.redis = redis;
        this.emailService = emailService;
    }

    @Transactional 
    @KafkaListener(topics = "user-analized", groupId = "smurffing")
    public void analyzingSmurffing(UserBehaviorAnalyzedEvent event) {
        if (event.account().getAccountType().getAccountType().equals("fixed_deposit_account")) return;
        logger.info("Evaluating transactions from the last minutes");
        List<Transaction> transactionsMinute2 = transactionRepository
            .findByAccountAndTransactionDateBeforeOrderByTransactionDateDesc(
                event.account(), 
                LocalDateTime.now().minusMinutes(2)
            );
        List<Transaction> transactionsMinute1 = transactionRepository
            .findByAccountAndTransactionDateBeforeOrderByTransactionDateDesc(
                event.account(), 
                LocalDateTime.now().minusMinutes(2)
            );
        if (transactionsMinute2.size() >= 7 || transactionsMinute1.size() >= 5) {
            logger.info("Warning: Suspicious Bot Activity Detected");
            logger.info("Add the banned user to Redis");
            redis.opsForValue().set(
                "banned-account-" + event.account().getId(), 
                "Sanctioned for bots", 
                Duration.ofMinutes(15)
            );
            logger.info("Add the account to black list");
            event.account().setAccountStatus(AccountStatus.frozen);
            accountRepository.save(event.account());
            blackListRepository.save(new BlackList(event.account()));
        }  
        logger.info("The user passed the bot test"); 
        emailService.penalizationEmail(event.account(), null, "ban", "use of bots");
    } 
}
