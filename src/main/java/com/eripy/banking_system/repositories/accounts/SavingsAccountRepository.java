package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.SavingsAccount;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long>{

}