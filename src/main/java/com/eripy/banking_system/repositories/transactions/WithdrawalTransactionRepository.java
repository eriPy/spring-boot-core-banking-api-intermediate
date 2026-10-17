package com.eripy.banking_system.repositories.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.transactions.WithdrawalTransaction;

public interface WithdrawalTransactionRepository extends JpaRepository<WithdrawalTransaction, Long> {

}