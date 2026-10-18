package com.eripy.banking_system.dto;

import java.math.BigDecimal;

public record AccountGeneral(
    Long id,
    String accountCard,
    BigDecimal balance,
    BigDecimal monthlyEarnings,
    String accountType
) {}
