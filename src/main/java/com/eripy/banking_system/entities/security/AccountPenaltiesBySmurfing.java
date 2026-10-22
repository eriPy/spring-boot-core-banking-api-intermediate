package com.eripy.banking_system.entities.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "account_penalties_by_smurfing")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AccountPenaltiesBySmurfing {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "history_id", nullable = false, unique = true)
    @NotNull(message = "The history references is required")
    private History history;

    @Column(name = "warnings", nullable = false)
    @Min(value = 0, message = "Warnings must be 1 or greater")
    @Max(value = 9, message = "Warnings cannot exceed 9")
    private int warnings;

    @Column(name = "suspensions", updatable = false)
    @Min(value = 0, message = "Suspensions must be 0 or greater")
    @Max(value = 2, message = "Suspensions cannot exceed 2")
    private int suspensions;

    public AccountPenaltiesBySmurfing(
        History history,
        int warnings
    ) {
        this.history = history;
        this.warnings = warnings;
    }

    public AccountPenaltiesBySmurfing(
        History history,
        int warnings,
        int suspensions
    ) {
        this(history, warnings);
        this.suspensions = suspensions;
    }
}