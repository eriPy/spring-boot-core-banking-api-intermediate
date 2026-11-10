package com.eripy.banking_system.service.scheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.Credit;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.transactions.InterestAccrualTransaction;
import com.eripy.banking_system.entities.transactions.InterestCreditTransaction;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.transactions.InterestAccrualTransactionRepository;
import com.eripy.banking_system.repositories.transactions.InterestCreditTransactionRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class TransactionInterest {
    private final RedisTemplate<String, String> redis;
    private final CheckingAccountRepository checkingAccountRepository;
    private final InterestCreditTransactionRepository interestCreditTransactionRepository;
    private final InterestAccrualTransactionRepository interestAccrualTransactionRepository;
    private final TransactionRepository transactionRepository;
    private final GetterService getterService;
    private static final Logger logger = LoggerFactory.getLogger(TransactionInterest.class);

    public TransactionInterest(
        RedisTemplate<String, String> redis,
        CheckingAccountRepository checkingAccountRepository,
        InterestCreditTransactionRepository interestCreditTransactionRepository,
        InterestAccrualTransactionRepository interestAccrualTransactionRepository,
        TransactionRepository transactionRepository,
        GetterService getterService
    ) {
        this.redis = redis;
        this.checkingAccountRepository = checkingAccountRepository;
        this.interestCreditTransactionRepository = interestCreditTransactionRepository;
        this.interestAccrualTransactionRepository = interestAccrualTransactionRepository;
        this.transactionRepository = transactionRepository;
        this.getterService = getterService;
    }

    @Transactional 
    public <T extends Credit> boolean increase(
        JpaRepository<T, Long> dataFetcher,
        int pageNumber,
        String key,
        int pageSize
    ) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
        Page<T> page = dataFetcher.findAll(pageRequest);
        List<T> accounts = page.getContent();
        for (T sa: accounts) {
            BigDecimal currentBalance = sa.getAccount().getBalance();
            BigDecimal newBalance = currentBalance.multiply(sa.getCreditInterestRate()).add(currentBalance);
            BigDecimal bussinesBalance = newBalance.setScale(2, RoundingMode.HALF_EVEN);
            sa.getAccount().setBalance(bussinesBalance);
            Transaction t = new Transaction(
                sa.getAccount(),
                getterService.getTransactionType("interest_credit"),
                newBalance.subtract(currentBalance)
            );
            transactionRepository.save(t);
            interestCreditTransactionRepository.save(new InterestCreditTransaction(
                t, 
                getterService.getPaymentMethod("system")
            ));
        }  
        dataFetcher.saveAll(accounts);
        redis.opsForValue().set(key, String.valueOf(pageNumber));
        return page.hasNext();
    }

    @Transactional 
    public boolean debit(String key, int pageNumber, int pageSize) {
        PageRequest pageRequest = PageRequest.of(pageNumber, pageSize, Sort.by("id").ascending());
        Page<CheckingAccount> page = checkingAccountRepository.findAll(pageRequest);
        List<CheckingAccount> accounts = page.getContent();
        for (CheckingAccount account: accounts) {
            BigDecimal previousOverdraft = account.getOverdraftUsed();
            BigDecimal previousInterest = account.getInterestPayable();
            BigDecimal currentInterest = account.getInterestPayable().add(account.getOverdraftUsed());
            BigDecimal newInterest = currentInterest.multiply(account.getOverdraftInterestRate());
            BigDecimal totalInterest = currentInterest.add(newInterest).setScale(2, RoundingMode.HALF_EVEN);
            PersonType personType = account.getAccount().getUser().getPersonType();
            BigDecimal newOverdraftLimit = getterService.getOverdraft(personType).getOverdraftValue();
            account.setOverdraftLimit(newOverdraftLimit);
            account.setOverdraftUsed(BigDecimal.ZERO);
            account.setInterestPayable(totalInterest);
            Transaction t = new Transaction(
                account.getAccount(),
                getterService.getTransactionType("interest_accrual"),
                totalInterest.subtract(previousInterest.add(previousOverdraft))
            );
            transactionRepository.save(t);
            interestAccrualTransactionRepository.save(new InterestAccrualTransaction(
                t, 
                getterService.getPaymentMethod("system"), 
                previousOverdraft, 
                account.getOverdraftUsed(), 
                previousInterest, 
                account.getInterestPayable()
            ));
        }
        logger.info("Account and transaction processed");
        checkingAccountRepository.saveAll(accounts);
        redis.opsForValue().set(key, String.valueOf(pageNumber));
        return page.hasNext();
    }
}
