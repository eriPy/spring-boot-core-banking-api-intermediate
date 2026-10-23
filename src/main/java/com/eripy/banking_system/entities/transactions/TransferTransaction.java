package com.eripy.banking_system.entities.transactions;

import java.math.BigDecimal;
import com.eripy.banking_system.dto.transactions.TransactionDetail;
import com.eripy.banking_system.entities.accounts.Account;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity 
@Table(name = "transfer_transactions")
@Getter 
@Setter 
@NoArgsConstructor 
@AllArgsConstructor 
public class TransferTransaction implements TransactionDetail {
    @Id 
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne 
    @JoinColumn(name = "transaction_id", nullable = false, updatable = false, unique = true)
    @NotNull(message = "The transaction references is required")
    private Transaction transaction;

    @Column(name = "previous_balance_payer", insertable = false, updatable = false)
    private BigDecimal previousBalancePayer;

    @Column(name = "ending_balance_payer", insertable = false, updatable = false)
    private BigDecimal endingBalancePayer;

    @ManyToOne
    @JoinColumn(name = "payee_id", updatable = false, nullable = false)
    private Account payee;

    @Column(name = "previous_balance_payee", insertable = false, updatable = false)
    private BigDecimal previousBalancePayee;
    
    @Column(name = "ending_balance_payee", insertable = false, updatable = false)
    private BigDecimal endingBalancePayee;

    @ManyToOne 
    @JoinColumn(name = "payment_method_id", updatable = false, nullable = false)
    @NotNull(message = "The payment method is required")
    private PaymentMethod paymentMethod;
}