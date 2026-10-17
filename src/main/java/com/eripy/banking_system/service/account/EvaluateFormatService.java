package com.eripy.banking_system.service.account;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.exceptions.FormatDataException;

@Service 
public class EvaluateFormatService {
    private static final Logger logger = LoggerFactory.getLogger(EvaluateFormatService.class);

    public boolean evaluateAccountType(String accountType, List<AccountsType> types) {
        logger.info("Fetching data format");
        return types.stream()
             .anyMatch(type -> type.getAccountType().equals(accountType));
    }

    public boolean evaluatePersonType(String personType, List<PersonType> types) {
        logger.info("Fetching data format");
        return types.stream()
            .anyMatch(type -> type.getPersonType().equals(personType));
    }

    public AccountsType getAccountTypeObject(String accountType, List<AccountsType> list) {
        for (AccountsType type: list) {
            if (type.getAccountType().equals(accountType)) {
                return type;
            }
        }
        logger.error("Something went wrong to get the accoun type object");
        throw new FormatDataException("Problem");
    }

    public PersonType getPersonTypeObject(String accountType, List<PersonType> list) {
        for (PersonType type: list) {
            if (type.getPersonType().equals(accountType)) {
                return type;
            }
        }
        logger.error("Something went wrong to get the accoun type object");
        throw new FormatDataException("Problem");
    }
}
