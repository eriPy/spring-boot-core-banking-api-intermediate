package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.TokenType;

public interface TokenTypeRepository extends JpaRepository<TokenType, Long> {
    Optional<TokenType> findByTokenType(String tokenType);
}