package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.transactions.DepositRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.InterestAccrualTransaction;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.repositories.transactions.InterestAccrualTransactionRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class InterestService {
    private final InterestAccrualTransactionRepository interestAccrualTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final GetterService getterService;
    private static final Logger logger = LoggerFactory.getLogger(InterestService.class);

    public InterestService(
        InterestAccrualTransactionRepository interestAccrualTransactionRepository,
        TransactionRepository transactionRepository,
        GetterService getterService
    ) {
        this.interestAccrualTransactionRepository = interestAccrualTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.getterService = getterService;
    }

    public void processOverdraftUsed(
        Account account, 
        BigDecimal interestCharged,
        BigDecimal oldOverdraft,
        DepositRequest request,
        BigDecimal newInterest,
        BigDecimal newOverdraft,
        BigDecimal interestsPayable
    ) {
        Transaction t = new Transaction(
            account,
            getterService.getTransactionType("interest_charge"),
            interestCharged
        );
        transactionRepository.save(t);
        interestAccrualTransactionRepository.save(new InterestAccrualTransaction(
            t, 
            getterService.getPaymentMethod(request.paymentMethod()), 
            oldOverdraft, 
            newOverdraft, 
            interestsPayable, 
            newInterest
        ));
        logger.info("Interest transaction processed");
    }
}
