package com.eripy.banking_system.service.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.SignUpRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.exceptions.FormatDataException;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;

@Service 
public class SecurityService {
    private final AccountRepository accountRepository;
    private static final Logger logger = LoggerFactory.getLogger(SecurityService.class);

    public SecurityService(
        AccountRepository accountRepository
    ) {
        this.accountRepository = accountRepository;
    }

    public void checkDataConsistency(
        SignUpRequest data, AccountsType accountType, PersonType student, PersonType unemployed
    ) {
        logger.info("Search the user data on database");
        boolean resEmail = accountRepository.existsByEmail(data.email());
        boolean resIdCard = accountRepository.existsByIdCard(data.idCard());
        boolean resUser = accountRepository.existsByEmailAndIdCard(data.email(), data.idCard());
        logger.info("Evaluanting consistence data with the person type");
        if ((data.personType().equals("unemployed") || data.personType().equals("student")) 
            && data.accountType().equals("payroll_account")
        ) {
            logger.warn("Someone who is unemployed or a student cannot have a payroll account");
            throw new InconsistenceDataException("The data is inconsistence");
        }
        if (resEmail ^ resIdCard) {
            logger.warn("One piece of data exists, but the other does not");
            throw new InconsistenceDataException("The data is inconsistence");

        } else if (resUser) {
            logger.info("The user information was found");
            logger.info("Search inconsistences data");
            boolean isStudent = accountRepository.existsByEmailAndIdCardAndPersonType(
                data.email(), data.idCard(), student
            );
            boolean isUnemployed = accountRepository.existsByEmailAndIdCardAndPersonType(
                data.email(), data.idCard(), unemployed
            );
            if ((isStudent || isUnemployed) && data.accountType().equals("payroll_account")) {
                logger.warn("Inconsistences found");
                throw new InconsistenceDataException("The data has problems");
            }
            if (accountRepository.existsByEmailAndIdCardAndAccountType(
                data.email(), data.idCard(), accountType
            )) {
                logger.warn("An account with this format already exists");
                throw new InconsistenceDataException("The data has problems");
            }
            logger.info("The account has not incosistences");
        }
    }

    public void evaluateStatus(Account account) {
        if (account.getAccountStatus() != AccountStatus.active) {
            logger.info("The account cannot make transactions");
            throw new FormatDataException("Invalid data");
        }
    }
}