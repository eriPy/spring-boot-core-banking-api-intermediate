package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.service.GetterService;

public class TransferService {
    private final TransactionsInformationService transactionsInformationService;
    private final GetterService getterService;
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);
    
    public TransferService(
        TransactionsInformationService transactionsInformationService,
        GetterService getterService
    ) {
        this.transactionsInformationService = transactionsInformationService;
        this.getterService = getterService;
    }

    public void processToSavingAccount(Account account, BigDecimal amount) {
        logger.info("Evaluating conditions for a saving account");
        boolean addInterest = transactionsInformationService
            .calculateToSavingAccountTransactions(account);
        if (addInterest) {
            logger.info("Adding interest");
            BigDecimal fee = getterService
                .getExcessTransactionFee(account.getId());
            System.out.println(fee);
        }
    }   
}
