package com.eripy.banking_system.entities.accounts;

import java.math.BigDecimal;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "savings_accounts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class SavingsAccount {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne  
    @JoinColumn(name = "account_id", updatable = false, unique = true)
    @NotNull(message = "The account references is required")
    private Account account;

    @Column(name = "excess_transaction_fee", insertable = false, updatable = false)
    private BigDecimal excessTransactionFee;

    @Column(name = "credit_interest_rate", insertable = false, updatable = false)
    private BigDecimal creditInterestRate;

    @Column(name = "interest_receivable", nullable = false, insertable = false)
    @DecimalMin(value = "0.00", message = "The interest receivable cannot be least than 0")
    private BigDecimal interestReceivable;

    @Column(name = "transactions_without_fee", insertable = false)
    private int transactionsWithoutFee;

    public SavingsAccount(Account account) {
        this.account = account;
    }
}