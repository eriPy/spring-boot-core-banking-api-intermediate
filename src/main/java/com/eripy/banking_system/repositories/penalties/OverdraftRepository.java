package com.eripy.banking_system.repositories.penalties;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.security.Overdraft;

public interface OverdraftRepository extends JpaRepository<Overdraft, Long> {
    Optional<Overdraft> findByPersonType(PersonType personType);
}
