package com.eripy.banking_system.entities.security;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "penalized_reasons")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class PenalizedReason {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reason", insertable = false, updatable = false)
    private String reason;  
}