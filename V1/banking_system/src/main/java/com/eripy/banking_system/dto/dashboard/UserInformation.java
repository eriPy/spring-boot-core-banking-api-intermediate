package com.eripy.banking_system.dto.dashboard;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record UserInformation(
    String accountHolder,
    String email,
    BigDecimal monthlyEarnings,
    String persontType,
    BigDecimal overdraftLimit,
    BigDecimal overdraftUsed,
    BigDecimal accrualInterest,
    LocalDateTime maturityTime,
    List<String> accountTypes,
    List<String> accountCards,
    List<BigDecimal> balances,
    List<UserTransactions> userTransactions
) {}