package com.eripy.banking_system.service.account;

import java.time.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.SignUpRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.service.GetterService;

@Service 
public class SecurityService {
    private final RedisTemplate<String, String> redis;
    private final GetterService getterService;

    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    public SecurityService(
        RedisTemplate<String, String> redis,
        GetterService getterService
    ) {
        this.redis = redis;
        this.getterService = getterService;
    }

        public void checkDataConsistency(SignUpRequest data) {
            logger.info("Evaluanting consistence data with the person type");
            if ((data.personType().equals("unemployed") || data.personType().equals("student")) 
                && data.accountType().equals("payroll_account")
            ) {
                logger.warn("Someone who is unemployed or a student cannot have a payroll account");
                throw new InconsistenceDataException("The data is inconsistence");
            }
        }

    public void evaluateStatus(Account account) {
        if (account.getAccountStatus() != AccountStatus.active) {
            logger.info("The account cannot make transactions");
            throw new InconsistenceDataException("Invalid data");
        }
    }

    public void saveInRedis(Account account) {
        redis.opsForValue().set(
            "banned-account-" + account.getId(), 
            "Sanctioned for smurfing", 
            Duration.ofMinutes(15)
        );
    }

    public Account evaluateIfPass(Long id) {
        Account account = getterService.getAccountFromDatabase(id);
        evaluateStatus(account);
        String isBanned = redis.opsForValue().get("banned-account-" + account.getId());
        if (isBanned != null) {
            logger.warn("This account was banned");
            throw new InconsistenceDataException("Invalid data");
        }
        return account;
    }
}