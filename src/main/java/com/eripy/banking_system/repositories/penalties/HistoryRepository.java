package com.eripy.banking_system.repositories.penalties;

import org.springframework.data.jpa.repository.JpaRepository;
import com.eripy.banking_system.entities.security.History;

public interface HistoryRepository extends JpaRepository<History, Long> {

}