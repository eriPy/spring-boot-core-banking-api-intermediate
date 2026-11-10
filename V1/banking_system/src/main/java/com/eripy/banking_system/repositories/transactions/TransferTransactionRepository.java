package com.eripy.banking_system.repositories.transactions;

import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.transactions.TransferTransaction;

public interface TransferTransactionRepository extends JpaRepository<TransferTransaction, Long> {
    @Query("""
        SELECT tt
        FROM TransferTransaction tt
        JOIN tt.transaction t
        JOIN t.transactionType ttype
        WHERE ttype.transactionType = 'transfer'
        AND tt.payee = :payee
        AND t.transactionDate >= :transactionDate
        ORDER BY t.transactionDate DESC
    """)
    List<TransferTransaction> findLastPeriod(
        @Param("payee") Account payee,
        @Param("transactionDate") LocalDateTime transactionDate
    );
}