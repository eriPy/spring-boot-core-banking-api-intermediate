package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.PenalizedReason;

public interface PenalizedReasonRepository extends JpaRepository<PenalizedReason, Long> {
    Optional<PenalizedReason> findByReason(String reason);
}