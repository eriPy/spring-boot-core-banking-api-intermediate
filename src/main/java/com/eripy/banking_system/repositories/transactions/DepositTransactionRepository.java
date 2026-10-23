package com.eripy.banking_system.repositories.transactions;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.DepositTransaction;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {
    @Query("""
        SELECT dt
        FROM DepositTransaction dt
        JOIN dt.transaction t
        JOIN t.transactionType ttype
        WHERE t.transactionDate >= :transactionDate
        AND ttype.transactionType = 'deposit'
        AND t.account = :account
    """)
    List<DepositTransaction> findLastPeriod(
        @Param("transactionDate") LocalDateTime transactionDate,
        @Param("account") Account account
    );
}