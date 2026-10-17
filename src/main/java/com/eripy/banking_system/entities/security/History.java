package com.eripy.banking_system.entities.security;

import com.eripy.banking_system.entities.accounts.Account;
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
@Table(name = "history")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class History {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "account_id", insertable = false, updatable = false, unique = false)
    @NotNull(message = "The account references is required")
    private Account account;
}