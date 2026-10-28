package com.eripy.banking_system.service.scheduler;

import java.time.LocalDate;
import java.time.ZoneId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.repositories.accounts.FixedDepositAccountRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;

@Service 
public class UserInterest {
    private final SavingsAccountRepository savingsAccountRepository;
    private final FixedDepositAccountRepository fixedDepositAccountRepository;
    private final RedisTemplate<String, String> redis;
    private final TransactionInterest transactionInterest;
    private static final Logger logger = LoggerFactory.getLogger(UserInterest.class);
    private static final int PAGE_SIZE = 500;

    public UserInterest(
        SavingsAccountRepository savingsAccountRepository,
        FixedDepositAccountRepository fixedDepositAccountRepository,
        RedisTemplate<String, String> redis,
        TransactionInterest transactionInterest
    ) {
        this.savingsAccountRepository = savingsAccountRepository;
        this.fixedDepositAccountRepository = fixedDepositAccountRepository;
        this.redis = redis;
        this.transactionInterest = transactionInterest;
    }

    private int itWasRedis(String key) {
        try {
            logger.info("Search in redis if the transaction was process");
            String data = redis.opsForValue().get(key);
            if (data == null) {
                logger.info("The transaction has not been completed today.");
                return 0;
            }
            if (!data.equals("yes")) return Integer.parseInt(data);
            logger.info("The transaction has not yet been completed");
            return -1;
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return -1;
        }
    }

    private void increaseToSavingsAccount(int page) { 
        String key = "credit-payment-to-savings";
        int pageNumber = page;
        Boolean sufficientData = true;
        boolean end = true;
        while (sufficientData) {
            try {
                sufficientData = transactionInterest
                    .increase(savingsAccountRepository, pageNumber, key, PAGE_SIZE);
                pageNumber++;
            } catch (Exception e) {
                logger.warn("Something went wrong");
                redis.opsForValue().set(key, String.valueOf(pageNumber));
                end = false;
                break;
            }
        }
        if (end) {
            logger.info("Saving the operation");
            java.time.Instant midnight = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
            redis.opsForValue().set(key, "yes");
            redis.expireAt(key, midnight);
        }
    }

    private void increaseToFixedDepositAccount(int page) { 
        String key = "credit-payment-to-fixed-deposit";
        int pageNumber = page;
        Boolean sufficientData = true;
        boolean end = true;
        while (sufficientData) {
            try {
                sufficientData = transactionInterest
                    .increase(fixedDepositAccountRepository, pageNumber, key, PAGE_SIZE);
                pageNumber++;
            } catch (Exception e) {
                logger.warn("Failed to increase the credit interest to users", e.getMessage());
                redis.opsForValue().set(key, String.valueOf(pageNumber));
                end = false;
                break;
            }
        }
        if (end) {
            logger.info("Saving the operation");
            java.time.Instant midnight = LocalDate.now()
                .plusDays(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant();
            redis.opsForValue().set(key, "yes");
            redis.expireAt(key, midnight);
        }    
    }

    @Scheduled(cron = "0 0 */3 * * ?")
    @SchedulerLock(
        name = "credit_interest",
        lockAtLeastFor = "PT1M",
        lockAtMostFor = "PT10M"
    )
    public void increaseUserCreditInterest() {
        logger.info("Increasing the credit interest to user");
        int savings = itWasRedis("credit-payment-to-savings");
        if (savings >= 0) increaseToSavingsAccount(savings);
        int fixedDeposits = itWasRedis("credit-payment-to-fixed-deposit");
        if (fixedDeposits >= 0) increaseToFixedDepositAccount(fixedDeposits);
    }

    @Scheduled(cron = "0 0 */3 * * ?")
    @SchedulerLock(
        name = "debit_interest",
        lockAtLeastFor = "PT1M",
        lockAtMostFor = "PT10M"
    )
    public void increaseUserDebitInterest() {
        logger.info("Increasing the debit interest to user");
        String key = "debit-to-checking";
        int chekings = itWasRedis(key);
        int pageNumber = 0;
        boolean sufficientData = true;
        boolean end = true;
        if (chekings >= 0) {
            while (sufficientData) {
                try {
                    sufficientData = transactionInterest
                        .debit(key, pageNumber, PAGE_SIZE);
                    pageNumber++;
                } catch (Exception e) {
                    logger.warn("Something went wrong", e.getMessage());
                    redis.opsForValue().set(key, String.valueOf(pageNumber));
                    end = false;
                    break;
                }
            }
            if (end) {
                logger.info("Debit interest increase for all user");
                java.time.Instant midnight = LocalDate.now()
                    .plusDays(1)
                    .atStartOfDay(ZoneId.systemDefault())
                    .toInstant();
                redis.opsForValue().set(key, "yes");
                redis.expireAt(key, midnight);
            }
        }
    }
}
