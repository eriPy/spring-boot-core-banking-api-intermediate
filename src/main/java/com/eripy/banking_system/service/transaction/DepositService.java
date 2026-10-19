package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.DepositRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.EmailService;

@Service 
public class DepositService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(DepositService.class);
    
    public DepositService(
        GetterService getterService,
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        EmailService emailService
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
    }

    @Transactional 
    public void processDeposit(Long id, DepositRequest depositRequest) {
        logger.info("Search user");
        logger.info("User found");
        logger.info("Search account information");
        Account account = getterService.getAccountFromDatabase(id);
        String act = account.getAccountType().getAccountType();
        if (act.equals("fixed_deposit_account")) {
            processToFixedDepositAccount(account);
        }
        logger.info("Process the deposit");
        account.setBalance(account.getBalance().add(depositRequest.amount()));
        accountRepository.save(account);
        logger.info("Deposit successful");
        Transaction transaction = new Transaction(
            account,
            getterService.getTransactionType("deposit"),
            depositRequest.amount()
        );
        transactionRepository.save(transaction);
        emailService.sendEmail(
            account.getEmail(), 
            "Deposit successful", 
            "The deposit for $" + depositRequest.amount() + " has been made"
        );
    }

    public void processToFixedDepositAccount(Account account) {
        logger.info("Evaluating conditions for a fixed deposit account");
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn(
                "A fixed deposit account cannot receive further deposits once a balance is introduced"
            );
            throw new InconsistenceDataException("Account problem");
        }
        logger.info("The account can still proceed with the deposit");
    }
}
