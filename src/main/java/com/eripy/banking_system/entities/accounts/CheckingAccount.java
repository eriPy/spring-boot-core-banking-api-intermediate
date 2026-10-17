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
@Table(name = "checking_accounts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class CheckingAccount {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne  
    @JoinColumn(name = "account_id", updatable = false, unique = true)
    @NotNull(message = "The account references is required")
    private Account account;

    @Column(name = "overdraft_limit", insertable = false, updatable = false)
    private BigDecimal overdraftLimit;

    @Column(name = "overdraft_used", insertable = false)
    @DecimalMin(value = "0.00", message = "The overdraft cannot be least than 0")
    private BigDecimal overdraftUsed;

    @Column(name = "overdraft_interest_rate", insertable = false, updatable = false)
    private BigDecimal overdraftInterestRate;

    @Column(name = "overdraft_interest_rate", insertable = false, updatable = false)
    private BigDecimal managementFee;

    @Column(name = "interest_payable", insertable = false)
    private BigDecimal interestPayable;

    public CheckingAccount(Account account) {
        this.account = account;
    }
}