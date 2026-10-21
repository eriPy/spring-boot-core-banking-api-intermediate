package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.security.OperationToken;
import com.eripy.banking_system.entities.security.TokenType;

public interface OperationTokenRepository extends JpaRepository<OperationToken, Long> {
    @Query("""
        SELECT ot
        FROM OperationToken ot
        JOIN ot.account a
        JOIN a.accountType atp
        WHERE ot.token = :token
        AND atp.accountType = :accountType
        AND a = :account
        AND ot.tokenType = :tokenType
        ORDER BY ot.createdAt DESC   
    """)
    Optional<OperationToken> findByLastestToken(
        @Param("token") String token,
        @Param("accountType") String accountType,
        @Param("account") Account account,
        @Param("tokenType") TokenType tokenType
    );
}