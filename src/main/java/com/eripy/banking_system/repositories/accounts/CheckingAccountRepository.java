package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.CheckingAccount;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long>{

}