package com.eripy.banking_system.service.account;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.security.PenalizedAccount;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.penalties.PenalizedAccountRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class AccountService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final EvaluateFormatService evaluateFormatService;
    private final PenalizedAccountRepository penalizedAccountRepository;
    private final BlackListRepository blackListRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(
        GetterService getterService,
        AccountRepository accountRepository,
        EvaluateFormatService evaluateFormatService,
        PenalizedAccountRepository penalizedAccountRepository,
        BlackListRepository blackListRepository
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.evaluateFormatService = evaluateFormatService;
        this.penalizedAccountRepository = penalizedAccountRepository;
        this.blackListRepository = blackListRepository;
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

    public void unfreeze(Account account) {
        logger.info("Search if user is freezed");
        if (account.getAccountStatus() == AccountStatus.frozen) {
            PenalizedAccount pa = penalizedAccountRepository
                .findFreezed(account, LocalDateTime.now())
                .orElse(null);
            if (pa == null) {
                logger.info("The user's account can now be unfrozen");
                account.setAccountStatus(AccountStatus.active);
                accountRepository.save(account);
                return;
            }
            logger.info("The user's account must remain frozen");
        }
    }

    public void inBlackList(String email, String idCard) {
        logger.info("Search if user in black list");
        if (blackListRepository.existsByAccountEmailAndAccountIdCard(email, idCard)) {
            logger.info("This user is on the blacklist");
            throw new InconsistenceDataException("Invalid access");
        }
    }
}
