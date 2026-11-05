package com.eripy.banking_system.repositories.ledger;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.ledger.Ledger;

public interface LedgerRepository extends JpaRepository<Ledger, Long>{
}