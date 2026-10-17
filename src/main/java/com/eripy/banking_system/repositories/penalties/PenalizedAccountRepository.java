package com.eripy.banking_system.repositories.penalties;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.PenalizedAccount;

public interface PenalizedAccountRepository extends JpaRepository<PenalizedAccount, Long> {

}