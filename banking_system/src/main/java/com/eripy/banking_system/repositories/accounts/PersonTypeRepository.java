package com.eripy.banking_system.repositories.accounts;

import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.accounts.PersonType;

public interface PersonTypeRepository extends JpaRepository<PersonType, Long>{
    @EntityGraph(attributePaths = {"overdraft"})
    Optional<PersonType> findByPersonType(String personType);
}