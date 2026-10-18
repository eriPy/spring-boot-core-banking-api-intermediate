package com.eripy.banking_system.service;

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
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.AccountsTypeRepository;
import com.eripy.banking_system.repositories.accounts.PersonTypeRepository;
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
    private static final Logger logger = LoggerFactory.getLogger(GetterService.class);

    public GetterService(
        RedisTemplate<String, String> redis,
        ObjectMapper mapper,
        AccountsTypeRepository accountsTypeRepository,
        PersonTypeRepository personTypeRepository,
        AccountRepository accountRepository
    ) {
        this.redis = redis;
        this.mapper = mapper;
        this.accountsTypeRepository = accountsTypeRepository;
        this.personTypeRepository = personTypeRepository;
        this.accountRepository = accountRepository;
    }

    public List<AccountsType> getAccountTypes() {
        try {
            String data = redis.opsForValue().get("account-types");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                List<AccountsType> accountsTypes = accountsTypeRepository.findAll();
                redis.opsForValue().set(
                    "account-types",
                    mapper.writeValueAsString(accountsTypes),
                    Duration.ofHours(12)
                );
                return accountsTypes;
            }
            logger.info("Data found in Redis");
            return mapper.readValue(data, new TypeReference<List<AccountsType>>() {});

        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return accountsTypeRepository.findAll();

        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return accountsTypeRepository.findAll();
        }
    }

    public List<PersonType> getPersonType() {
        try {
            String data = redis.opsForValue().get("person-types");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                List<PersonType> personTypes = personTypeRepository.findAll();
                redis.opsForValue().set(
                    "person-types", 
                    mapper.writeValueAsString(personTypes),
                    Duration.ofHours(12)
                );
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

    public List<Account> getInformation(String email) {
        try {
            logger.info("Search information in Redis");
            String data = redis.opsForValue().get("user-" + email + "-session");
            if (data == null) {
                logger.info("Information not yet found in Redis");
                logger.info("Search information on database");
                List<Account> accountsInformation = accountRepository.findByEmail(email);
                redis.opsForValue().set(
                    "user-" + email + "-session", 
                    mapper.writeValueAsString(accountsInformation),
                    Duration.ofHours(12)
                );
                logger.info("Information saved in edis");
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
}
