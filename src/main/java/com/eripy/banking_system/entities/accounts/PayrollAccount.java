package com.eripy.banking_system.entities.accounts;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "payroll_accounts")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class PayrollAccount {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "account_id", updatable = false, unique = true)
    @NotNull(message = "The account references is required")
    private Account account;

    public PayrollAccount(Account account) {
        this.account = account;
    }
}