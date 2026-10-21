package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;
import java.util.List;
import com.eripy.banking_system.exceptions.FormatDataException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record DepositRequest(
    @NotNull(message = "The amount is required")
    @DecimalMin(value = "0.01", message = "The deposit must be least 0.01")
    BigDecimal amount,
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must has 6 characters long")
    String token,
    @NotBlank(message = "The payment is required")
    String paymentMethod
) {
    public DepositRequest {
        List<String> valid = List.of("cash", "atm", "bank_teller", "check");
        if (paymentMethod == null || !valid.contains(paymentMethod)) {
            throw new FormatDataException("Invalid payment method");
        }
    }
}
