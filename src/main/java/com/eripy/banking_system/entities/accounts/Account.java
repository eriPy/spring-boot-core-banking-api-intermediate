package com.eripy.banking_system.entities.accounts;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Email;
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
            columnNames = {"id_card", "accounts_type_id"}
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

    @Column(name = "account_holder", nullable = false)
    @NotBlank(message = "The account holder is required")
    @Size(max = 50, message = "The account holder is to long")
    private String accountHolder;

    @Column(name = "id_card", nullable = false, length = 10, updatable = false)
    @Size(max = 10, min = 10, message = "The ID must be 10 characters long")
    private String idCard;

    @Column(name = "email", nullable = false, unique = true)
    @NotBlank(message = "The email is required")
    @Email(message = "Invalid email format")
    @Size(max = 254)
    private String email;

    @Column(name = "account_password", nullable = false)
    @NotBlank(message = "The password is required")
    @Size(max = 255)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "account_status", nullable = false, insertable = false)  
    private AccountStatus accountStatus;

    @ManyToOne 
    @JoinColumn(name = "accounts_type_id", nullable = false)
    @NotNull(message = "The account type is required in an account")
    private AccountsType accountType;

    @Column(name = "balance", nullable = false, insertable = false)
    @DecimalMin(value = "0.00", message = "The balance cannot be least than 0")
    private BigDecimal balance;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createAt;

    @Column(name = "account_card", nullable = false, unique = true, length = 12, updatable = false)
    @NotBlank(message = "The account card is required in an account")
    @Size(min = 12, max = 12, message = "The account card must be 12 characters long")
    private String accountCard;

    @ManyToOne 
    @JoinColumn(name = "person_type_id", nullable = false)
    @NotNull(message = "The person type is required in an account")
    private PersonType personType;

    @Column(name = "monthly_earnings", nullable = false)
    @DecimalMin(value = "0.01", message = "You can't choose anything below zero")
    private BigDecimal monthlyEarnings;

    public Account(
        String accountHolder,
        String idCard,
        String email,
        String password,
        AccountsType accountsType,
        String accountCard,
        PersonType personType,
        BigDecimal monthlyEarnings
    ) {
        this.accountHolder = accountHolder;
        this.idCard = idCard;
        this.email = email;
        this.password = password;
        this.accountType = accountsType;
        this.accountCard = accountCard;
        this.personType = personType;
        this.monthlyEarnings = monthlyEarnings;
    }
}