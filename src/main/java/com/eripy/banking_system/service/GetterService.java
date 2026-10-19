package com.eripy.banking_system.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.entities.transactions.TransactionType;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.AccountsTypeRepository;
import com.eripy.banking_system.repositories.accounts.PersonTypeRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
import com.eripy.banking_system.repositories.transactions.TransactionTypeRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service  
public class GetterService {
    private final RedisTemplate<String, String> redis;
    private final ObjectMapper mapper;
    private final AccountsTypeRepository accountsTypeRepository;
    private final PersonTypeRepository personTypeRepository;
    private final AccountRepository accountRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private static final Logger logger = LoggerFactory.getLogger(GetterService.class);

    public GetterService(
        RedisTemplate<String, String> redis,
        ObjectMapper mapper,
        AccountsTypeRepository accountsTypeRepository,
        PersonTypeRepository personTypeRepository,
        AccountRepository accountRepository,
        TransactionTypeRepository transactionTypeRepository,
        SavingsAccountRepository savingsAccountRepository
    ) {
        this.redis = redis;
        this.mapper = mapper;
        this.accountsTypeRepository = accountsTypeRepository;
        this.personTypeRepository = personTypeRepository;
        this.accountRepository = accountRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.savingsAccountRepository = savingsAccountRepository;
    }

    public <T> void saveToRedis(String key, T data, Duration duration) throws JacksonException {
        redis.opsForValue().set(key, mapper.writeValueAsString(data), duration);
    }

    public List<AccountsType> getAccountTypes() {
        try {
            String data = redis.opsForValue().get("account-types");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                List<AccountsType> accountsTypes = getAccountTypesFromDatabase();
                logger.info("Retrieving data from a database");
                saveToRedis("account-types", accountsTypes, Duration.ofHours(12));
                logger.info("Data saved in Redis");
                return accountsTypes;
            }
            logger.info("Data found in Redis");
            return mapper.readValue(data, new TypeReference<List<AccountsType>>() {});

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return getAccountTypesFromDatabase();
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return getAccountTypesFromDatabase();
        }
    }

    private  List<AccountsType> getAccountTypesFromDatabase() {
        return accountsTypeRepository.findAll();
    }

    public List<PersonType> getPersonType() {
        try {
            String data = redis.opsForValue().get("person-types");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                List<PersonType> personTypes = personTypeRepository.findAll();
                logger.info("Retrieving data from a database:");
                saveToRedis("person-types", personTypes, Duration.ofHours(12));
                logger.info("Data saved in Redis");
                return personTypes;
            }
            logger.info("Data found in Redis");
            return mapper.readValue(data, new TypeReference<List<PersonType>>() {});
            
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return personTypeRepository.findAll();
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return personTypeRepository.findAll();
        }
    }

    public TransactionType getTransactionType(String type) {
        try {
            logger.info("Search the transaction type in Redis");
            String data = redis.opsForValue().get(type + "-transactions-object");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                TransactionType tt = getTransactionTypeFromDatabase(type);
                logger.info("Retrieving data from a database:");
                saveToRedis(type + "-transactions-object", tt, Duration.ofHours(12));
                logger.info("Data saved in Redis");
                return tt;
            }
            logger.info("Transaction type found in Redis");
            return mapper.readValue(data, new TypeReference<TransactionType>() {});

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return getTransactionTypeFromDatabase(type);
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return getTransactionTypeFromDatabase(type);
        }
    }

    private TransactionType getTransactionTypeFromDatabase(String type) {
        return transactionTypeRepository.findByTransactionType(type)
            .orElseThrow(() -> {
                logger.warn("Data not found on database");
                throw new ResourceNotFoundException("Not found");
            });
    }

    public List<Account> getInformation(String email) {
        try {
            logger.info("Search information in Redis");
            String data = redis.opsForValue().get("user-" + email + "-session");
            if (data == null) {
                logger.info("Information not yet found in Redis");
                logger.info("Search information on database");
                List<Account> accountsInformation = accountRepository.findByEmail(email);
                saveToRedis("user-" + email + "-session", accountsInformation, Duration.ofHours(12));
                logger.info("Information saved in redis");
                return accountsInformation;
            }
            logger.info("User information found in redis");
            return mapper.readValue(data, new TypeReference<List<Account>>() {});

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return accountRepository.findByEmail(email);
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return accountRepository.findByEmail(email);
        }
    }

    public Account getAccountFromDatabase(Long id) {
        return accountRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found");
                throw new ResourceNotFoundException("Account not found");
            });
    }

    public BigDecimal getExcessTransactionFee(Long id) {
        try {
            logger.info("Search account in Redis");
            String data = redis.opsForValue().get("excess-transactions-fee");
            if (data == null) {
                logger.info("Data not found in Redis");
                BigDecimal fee = getExcessTransactionFeeFromDatabase(id);
                logger.info("Search data on database");
                saveToRedis("excess-transactions-fee", fee, Duration.ofHours(24));
                logger.info("Data saved in Redis");
                return fee;
            }
            logger.info("Data found in Redis");
            return mapper.readValue(data, new TypeReference<BigDecimal>() {});

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return getExcessTransactionFeeFromDatabase(id);
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return getExcessTransactionFeeFromDatabase(id);
        }
    }

    private BigDecimal getExcessTransactionFeeFromDatabase(Long id) {
        SavingsAccount sa = savingsAccountRepository.findById(id)
            .orElseThrow(() -> {
                logger.warn("User not found");
                throw new ResourceNotFoundException("Account not found");
            });
        return sa.getExcessTransactionFee();
    }
}
