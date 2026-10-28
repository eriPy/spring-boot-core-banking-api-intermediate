package com.eripy.banking_system.repositories.accounts;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.SavingsAccount;

public interface SavingsAccountRepository extends JpaRepository<SavingsAccount, Long>{
    @EntityGraph(attributePaths = {"account"})
    Page<SavingsAccount> findAll(Pageable pageable);
}