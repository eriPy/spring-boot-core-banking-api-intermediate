package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;
import java.util.List;

import com.eripy.banking_system.exceptions.FormatDataException;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record WithdrawalRequest(
    @NotNull 
    @DecimalMin(value = "0.01")
    BigDecimal amount,
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must has 6 characters long")
    String token,
    @NotBlank(message = "The payment method is required")
    String paymentMethod
) {
    public WithdrawalRequest {
        List<String> valid = List.of("cash", "atm", "bank_teller");
        if (paymentMethod == null || !valid.contains(paymentMethod)) {
            throw new FormatDataException("Invalid payment method");
        }
    }
}
