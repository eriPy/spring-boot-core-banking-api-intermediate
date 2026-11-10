package com.eripy.banking_system.repositories.transactions;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.transactions.TransactionType;

public interface TransactionTypeRepository extends JpaRepository<TransactionType, Long>{
    Optional<TransactionType> findByTransactionType(String transactionType);
}