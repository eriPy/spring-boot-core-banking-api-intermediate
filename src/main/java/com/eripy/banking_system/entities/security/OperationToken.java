package com.eripy.banking_system.entities.security;

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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "operation_tokens")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class OperationToken {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne 
    @JoinColumn(name = "account_id", nullable = false, updatable = false)
    @NotNull(message = "The account references is required")
    private Account account;

    @Column(name = "token", nullable = false, updatable = false, length = 6)
    @NotBlank(message = "The token is required")
    @Size(max = 6, min = 6, message = "The token must be 6 characteres lenght")
    private String token;

    @Column(name = "is_used", insertable = false)
    private boolean isUsed;

    @Column(name = "create_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne 
    @JoinColumn(name = "token_type_id", updatable = false, nullable = false)
    @NotNull(message = "The token type is required")
    private TokenType tokenType;
}