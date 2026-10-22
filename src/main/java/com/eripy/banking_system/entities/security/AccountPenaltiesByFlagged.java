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
@Table(name = "account_penalties_by_flagged")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AccountPenaltiesByFlagged {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "history_id", unique = true, updatable = false, nullable = false)
    @NotNull (message = "The history references is required")
    private History history;

    @Column(name = "suspicions", insertable = false, updatable = false)
    @Min(value = 0, message = "suspicions must be 1 or greater")
    @Max(value = 2, message = "suspicions cannot exceed 3")
    private int suspicions;
}