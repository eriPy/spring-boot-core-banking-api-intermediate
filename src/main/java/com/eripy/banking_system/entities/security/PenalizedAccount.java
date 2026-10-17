package com.eripy.banking_system.entities.security;

import java.time.LocalDateTime;
import com.eripy.banking_system.entities.accounts.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "penalized_accounts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class PenalizedAccount {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    @NotNull(message = "The account references is required")
    private Account account;

    @Column(name = "frozen_until", nullable = false, updatable = false)
    @NotNull(message = "The frozen time is required")
    @Future(message = "The account must be unfrozen in the future")
    private LocalDateTime frozenUntil;

    @ManyToOne 
    @JoinColumn(name = "reason_id", nullable = false, updatable = false)
    @NotNull(message = "The reason is required")
    private PenalizedReason reason;
}