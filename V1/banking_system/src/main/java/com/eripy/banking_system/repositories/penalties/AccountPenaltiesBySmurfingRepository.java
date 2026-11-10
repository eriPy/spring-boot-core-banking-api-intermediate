package com.eripy.banking_system.repositories.penalties;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.AccountPenaltiesBySmurfing;
import com.eripy.banking_system.entities.security.History;

public interface AccountPenaltiesBySmurfingRepository extends JpaRepository<AccountPenaltiesBySmurfing, Long> {
    boolean existsByHistory(History history);
    
    AccountPenaltiesBySmurfing findByHistory(History history);
}