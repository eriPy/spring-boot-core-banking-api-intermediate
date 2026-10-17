package com.eripy.banking_system.entities.accounts;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "accounts_types")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class AccountsType {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_type", insertable = false, updatable = false)
    @NotBlank(message = "The account type is required")
    private String accountType;

    public AccountsType(String accountType) {
        this.accountType = accountType;
    }
}