package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.AccountsType;

public interface AccountsTypeRepository extends JpaRepository<AccountsType, Long>{
}