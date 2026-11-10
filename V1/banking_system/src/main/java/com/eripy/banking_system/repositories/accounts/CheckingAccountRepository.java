package com.eripy.banking_system.repositories.accounts;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.CheckingAccount;

public interface CheckingAccountRepository extends JpaRepository<CheckingAccount, Long>{
    Optional<CheckingAccount> findByAccount(Account account);

    @EntityGraph(attributePaths = {"account"})
    Page<CheckingAccount> findAll(Pageable pageable);
}