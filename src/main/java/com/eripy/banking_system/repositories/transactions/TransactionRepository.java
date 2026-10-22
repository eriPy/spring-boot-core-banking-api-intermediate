package com.eripy.banking_system.repositories.transactions;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.Transaction;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findByAccountAndTransactionDateBeforeOrderByTransactionDateDesc(
        Account account,
        LocalDateTime transactionDate
    );
}