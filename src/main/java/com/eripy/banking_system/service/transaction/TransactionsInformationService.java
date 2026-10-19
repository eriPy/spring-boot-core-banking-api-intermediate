package com.eripy.banking_system.service.transaction;

import java.time.LocalDateTime;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.entities.transactions.TransactionType;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class TransactionsInformationService {
    private final TransactionRepository transactionRepository;
    private final GetterService getterService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionsInformationService.class);

    public TransactionsInformationService(
        TransactionRepository transactionRepository,
        GetterService getterService
    ) {
        this.transactionRepository = transactionRepository;
        this.getterService = getterService;
    }

    public boolean calculateToSavingAccountTransactions(Account account) {
        logger.info("Getting the transaction type");
        TransactionType tt = getterService.getTransactionType("deposit");
        logger.info("Search current month transactions");
        LocalDateTime month = LocalDateTime.now()
            .withDayOfMonth(1)
            .withHour(0)
            .withMinute(0)
            .withSecond(0)
            .withNano(0);
        List<Transaction> monthTransactions = transactionRepository
            .findByTransactionTypeAndAccountAndTransactionDateGreaterThanEqual(
                tt, 
                account, 
                month
        );
        return monthTransactions.size() >= 6;
    }
}
