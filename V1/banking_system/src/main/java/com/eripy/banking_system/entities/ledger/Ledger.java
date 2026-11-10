package com.eripy.banking_system.entities.ledger;

import java.math.BigDecimal;
import com.eripy.banking_system.entities.transactions.Transaction;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "ledger")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Ledger {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "The Transaction is required")
    @ManyToOne 
    @JoinColumn(name = "transaction_id", insertable = false, updatable = false)
    private Transaction transaction;

    @Column(name = "debit_concept", insertable =  false, updatable = false)
    @NotBlank(message = "The debit concept is required")
    private String debitConcept;

    @Column(name = "credit_concept", insertable =  false, updatable = false)
    @NotBlank(message = "The credit concept is required")
    private String creditConcept;

    @Column(name = "debit", insertable = false, updatable = false)
    @DecimalMin(value = "0.01", message = "The debit must be greater than 0")
    private BigDecimal debit;

    @Column(name = "credit", updatable = false, insertable = false)
    @DecimalMin(value = "0.01", message = "The credit must be greater than 0")
    private BigDecimal credit;
}