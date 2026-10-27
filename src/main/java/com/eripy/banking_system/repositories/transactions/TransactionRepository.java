package com.eripy.banking_system.repositories.transactions;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.entities.transactions.TransactionType;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    @Query("""
        SELECT t
        FROM Transaction t
        WHERE t.account = :account
        AND t.transactionType.transactionType IN ('withdrawal', 'deposit', 'transfer')
        AND t.transactionDate >= :transactionDate
    """)
    List<Transaction> findRecentTransactions(
        @Param("account") Account account,
        @Param("transactionDate") LocalDateTime transactionDate
    );

    List<Transaction> findByAccountAndTransactionTypeAndTransactionDateBeforeOrderByTransactionDateDesc(
        Account account,
        TransactionType transactionType,
        LocalDateTime transactionDate
    );
}