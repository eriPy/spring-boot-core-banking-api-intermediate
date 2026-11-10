package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.AccountPenaltiesByFlagged;
import com.eripy.banking_system.entities.security.History;

public interface AccountPenaltiesByFlaggedRepository extends JpaRepository<AccountPenaltiesByFlagged, Long> {
    boolean existsByHistory(History history);

    Optional<AccountPenaltiesByFlagged> findByHistory(History history);
}