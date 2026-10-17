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
    @JoinColumn(name = "history_id", insertable = false, unique = true, updatable = false)
    @NotNull(message = "The history references is required")
    private History history;

    @Column(name = "warnings", insertable = false, updatable = false)
    @Min(value = 0, message = "Warnings must be 1 or greater")
    @Max(value = 9, message = "Warnings cannot exceed 9")
    private int warnings;

    @Column(name = "suspensions", insertable = false, updatable = false)
    @Min(value = 0, message = "Suspensions must be 1 or greater")
    @Max(value = 3, message = "Suspensions cannot exceed 2")
    private int suspensions;
}