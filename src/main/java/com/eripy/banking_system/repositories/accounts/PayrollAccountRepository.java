package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.PayrollAccount;

public interface PayrollAccountRepository extends JpaRepository<PayrollAccount, Long>{

}