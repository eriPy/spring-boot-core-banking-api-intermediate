package com.eripy.banking_system.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record UserTransactions(
    String accountType,
    String transactionType,
    BigDecimal amount,
    LocalDateTime transactionDate,
    String payeeCard,
    String payerCard 
) {}
