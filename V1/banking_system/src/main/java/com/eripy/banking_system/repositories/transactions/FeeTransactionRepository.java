package com.eripy.banking_system.repositories.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.transactions.FeeTransaction;

public interface FeeTransactionRepository extends JpaRepository<FeeTransaction, Long> {

}