package com.eripy.banking_system.service.scheduler;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.Credit;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.transactions.InterestAccrualTransactionRepository;
import com.eripy.banking_system.repositories.transactions.InterestCreditTransactionRepository;

@Service 
public class TransactionInterest {
    private final RedisTemplate<String, String> redis;
    private final CheckingAccountRepository checkingAccountRepository;
    private final InterestCreditTransactionRepository interestCreditTransactionRepository;
    private final InterestAccrualTransactionRepository interestAccrualTransactionRepository;

    public TransactionInterest(
        RedisTemplate<String, String> redis,
        CheckingAccountRepository checkingAccountRepository,
        InterestCreditTransactionRepository interestCreditTransactionRepository,
        InterestAccrualTransactionRepository interestAccrualTransactionRepository
    ) {
        this.redis = redis;
        this.checkingAccountRepository = checkingAccountRepository;
        this.interestCreditTransactionRepository = interestCreditTransactionRepository;
        this.interestAccrualTransactionRepository = interestAccrualTransactionRepository;
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
            BigDecimal currentInterest = account.getInterestPayable().add(account.getOverdraftUsed());
            BigDecimal newInterest = currentInterest.multiply(account.getOverdraftInterestRate());
            BigDecimal totalInterest = currentInterest.add(newInterest).setScale(2, RoundingMode.HALF_EVEN);
            account.setOverdraftUsed(BigDecimal.ZERO);
            account.setInterestPayable(totalInterest);
        }
        checkingAccountRepository.saveAll(accounts);
        redis.opsForValue().set(key, String.valueOf(pageNumber));
        return page.hasNext();
    }
}
