package com.eripy.banking_system.entities.accounts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(
    name = "accounts",
    uniqueConstraints = {
        @UniqueConstraint(
            columnNames = {"user_id", "accounts_type_id"}
        )
    }
)
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class Account {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, updatable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, insertable = false)  
    private AccountStatus accountStatus;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "accounts_type_id", nullable = false)
    @NotNull(message = "The account type is required in an account")
    private AccountsType accountType;

    @Column(name = "balance", nullable = false, insertable = false)
    @DecimalMin(value = "0.00", message = "The balance cannot be least than 0")
    private BigDecimal balance;

    @Column(name = "account_card", nullable = false, unique = true, length = 12, updatable = false)
    @NotBlank(message = "The account card is required in an account")
    @Size(min = 12, max = 12, message = "The account card must be 12 characters long")
    private String accountCard;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    public Account(
        User user,
        AccountsType accountsType,
        String accountCard
    ) {
        this.user = user;
        this.accountType = accountsType;
        this.accountCard = accountCard;
    }
}