package com.eripy.banking_system.repositories.penalties;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.AccountPenaltiesByFlagged;
import com.eripy.banking_system.entities.security.History;

public interface AccountPenaltiesByFlaggedRepository extends JpaRepository<AccountPenaltiesByFlagged, Long> {
    boolean existsByHistory(History history);
}