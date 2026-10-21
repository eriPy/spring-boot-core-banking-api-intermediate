package com.eripy.banking_system.service.account;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class AccountService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final EvaluateFormatService evaluateFormatService;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(
        GetterService getterService,
        AccountRepository accountRepository,
        EvaluateFormatService evaluateFormatService
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.evaluateFormatService = evaluateFormatService;
    }

    public AccountInformationResponse getUserInformation(Long id) {
        logger.info("Search the user information");
        if (!accountRepository.existsById(id)) {
            logger.warn("The user not exists");
            throw new ResourceNotFoundException("Invalid token");
        }
        logger.info("Search the account");
        Account account = getterService.getAccountFromDatabase(id);
        List<Account> information = getterService.getInformation(account.getEmail());
        return evaluateFormatService.getInformationFormat(information);
    }
}
