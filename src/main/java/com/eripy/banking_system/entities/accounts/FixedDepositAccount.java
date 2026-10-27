package com.eripy.banking_system.entities.accounts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "fixed_deposit_account")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class FixedDepositAccount {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne  
    @JoinColumn(name = "account_id", updatable = false, unique = true)
    @NotNull (message = "The account references is required")
    private Account account;

    @Column(name = "maturity_date", nullable = false, updatable = false)
    @NotNull(message = "The maturity is required")
    @Future(message = "The maturity date must be in the future")
    private LocalDateTime maturiyDate;

    @Column(name = "credit_interest_rate", insertable = false, updatable = false)
    private BigDecimal creditInterestRate;

    public FixedDepositAccount(Account account, LocalDateTime maturityDate) {
        this.account = account;
        this.maturiyDate = maturityDate;
    }
}