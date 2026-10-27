package com.eripy.banking_system.dto.account;

import java.math.BigDecimal;

public record AccountGeneral(
    Long id,
    String accountCard,
    BigDecimal balance,
    BigDecimal monthlyEarnings,
    String accountType,
    String accountStatus
) {}
