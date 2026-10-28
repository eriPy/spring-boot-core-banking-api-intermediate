package com.eripy.banking_system.repositories.accounts;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;

public interface FixedDepositAccountRepository extends JpaRepository<FixedDepositAccount, Long>{
    Optional<FixedDepositAccount> findByAccount(Account account);

    @EntityGraph(attributePaths = {"account"})
    Page<FixedDepositAccount> findAll(Pageable pageable);
} 