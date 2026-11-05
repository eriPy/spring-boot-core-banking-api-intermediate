package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.security.History;

public interface HistoryRepository extends JpaRepository<History, Long> {
    boolean existsByAccount(Account account);

    Optional<History> findByAccount(Account account);
}