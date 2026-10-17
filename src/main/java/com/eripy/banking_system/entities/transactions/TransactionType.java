package com.eripy.banking_system.entities.transactions;

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
@Table(name = "transaction_types")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransactionType {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "transaction_type", insertable = false, updatable = false)
    private String transactionType;
}