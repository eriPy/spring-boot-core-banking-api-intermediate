package com.eripy.banking_system.repositories.transactions;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.transactions.InterestAccrualTransaction;

public interface InterestAccrualTransactionRepository extends JpaRepository<InterestAccrualTransaction, Long> {

}