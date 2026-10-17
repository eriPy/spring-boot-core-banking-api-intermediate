package com.eripy.banking_system.service;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;
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
    private static final Logger logger = LoggerFactory.getLogger(GetterService.class);

    public GetterService(
        RedisTemplate<String, String> redis,
        ObjectMapper mapper,
        AccountsTypeRepository accountsTypeRepository,
        PersonTypeRepository personTypeRepository
    ) {
        this.redis = redis;
        this.mapper = mapper;
        this.accountsTypeRepository = accountsTypeRepository;
        this.personTypeRepository = personTypeRepository;
    }

    public List<AccountsType> getAccountTypes() {
        try {
            String data = redis.opsForValue().get("account-types");
            if (data == null) {
                logger.info("Data not yet found in Redis");
                List<AccountsType> accountsTypes = accountsTypeRepository.findAll();
                redis.opsForValue().set(
                    "account-types",
                    mapper.writeValueAsString(accountsTypes)
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
                    mapper.writeValueAsString(personTypes)
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
}
