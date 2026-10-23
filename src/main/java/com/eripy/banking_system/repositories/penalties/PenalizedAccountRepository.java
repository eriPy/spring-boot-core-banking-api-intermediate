package com.eripy.banking_system.repositories.penalties;

import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.security.PenalizedAccount;

public interface PenalizedAccountRepository extends JpaRepository<PenalizedAccount, Long> {
    @Query("""
        SELECT pa
        FROM PenalizedAccount pa
        WHERE pa.account = :account
        AND pa.frozenUntil >= :now
        ORDER BY pa.frozenUntil DESC
    """)
    Optional<PenalizedAccount> findFreezed(
        @Param("account") Account account, 
        @Param("now") LocalDateTime now
    );
}