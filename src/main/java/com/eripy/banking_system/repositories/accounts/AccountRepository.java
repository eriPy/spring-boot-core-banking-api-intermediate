package com.eripy.banking_system.repositories.accounts;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.User;

public interface AccountRepository extends JpaRepository<Account, Long> {
    boolean existsByAccountCard(String accountCard);

    List<Account> findByUser(User user);
}