package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;

public record DepositRequest(
    @DecimalMin(value = "0.01", message = "The deposit must be least 0.01")
    BigDecimal amount
) {}
