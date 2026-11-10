package com.eripy.banking_system.dto.manageAccount;

import java.math.BigDecimal;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MonthlyEarnings(
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must be 6 characters long")
    String token,
    @DecimalMin(value = "0.01", message = "The monthly earnings cannot be less than 0.01")
    @NotNull(message = "The monthly earnings are required")
    BigDecimal monthlyEarnings
) {}
