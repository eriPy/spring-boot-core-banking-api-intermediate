package com.eripy.banking_system.service.account;

import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.eripy.banking_system.dto.AccountGeneral;
import com.eripy.banking_system.dto.AccountInformationResponse;
import com.eripy.banking_system.entities.accounts.Account;
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

    public AccountInformationResponse getInformationFormat(List<Account> accounts) {
        List<AccountGeneral> general = new ArrayList<>();
        for (Account account: accounts) {
            String uncensoredAccountCard = account.getAccountCard();
            String censoredAccountCard = "********" + uncensoredAccountCard
                .substring(uncensoredAccountCard.length() - 4);
            general.add(new AccountGeneral(
                account.getId(),
                censoredAccountCard,
                account.getBalance(),
                account.getMonthlyEarnings(),
                account.getAccountType().getAccountType()
            ));
        }
        String uncensoredIdCard = accounts.get(0).getIdCard();
        String censoredIdCard = "********-" + uncensoredIdCard.charAt(uncensoredIdCard.length() - 1);
        return new AccountInformationResponse(
            accounts.get(0).getAccountHolder(),
            accounts.get(0).getEmail(),
            censoredIdCard,
            accounts.get(0).getPersonType().getPersonType(),
            general
        );
    }
}
