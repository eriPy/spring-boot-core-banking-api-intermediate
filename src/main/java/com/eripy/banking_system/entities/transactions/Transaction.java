package com.eripy.banking_system.entities.transactions;

import java.math.BigDecimal;
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
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "transactions")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Transaction {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "account_id", updatable = false, nullable = false)
    @NotNull(message = "The account references is required")
    private Account account;

    @ManyToOne 
    @JoinColumn(name = "transaction_type_id", nullable = false, updatable = false)
    @NotNull(message = "The transaction type is required")
    private TransactionType transactionType;

    @Column(name = "amount", nullable = false, updatable = false)
    @DecimalMin(value = "0.01", message = "The amount must be greater than 0")
    private BigDecimal amount;

    @Column(name = "transaction_date", insertable = false, updatable = false)
    private LocalDateTime transactionDate;
}