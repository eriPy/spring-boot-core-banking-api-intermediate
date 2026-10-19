package com.eripy.banking_system.repositories.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.transactions.DepositTransaction;

public interface DepositTransactionRepository extends JpaRepository<DepositTransaction, Long> {
}