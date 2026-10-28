package com.eripy.banking_system.repositories.penalties;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.BlackList;

public interface BlackListRepository extends JpaRepository<BlackList, Long> {
    
    boolean existsByAccountEmailAndAccountIdCard(String email, String idCard);
} 
