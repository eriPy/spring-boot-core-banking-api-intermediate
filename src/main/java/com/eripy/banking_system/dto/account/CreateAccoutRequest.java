package com.eripy.banking_system.dto.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CreateAccoutRequest(
    @NotBlank 
    String token,
    @NotBlank(message = "The account holder is required")
    String accountHolder,
    @NotBlank(message = "The password is required")
    @Size(max = 64, min = 8, message = "The password must be between 8 and 64 characters long")
    String password,
    @DecimalMin(value = "0.01", message = "Cannot select a monthly earnings least than 0.01")
    BigDecimal monthlyEarnings,
    @Future(message = "The data must be in the future")
    LocalDateTime madureTime
) {
    public CreateAccoutRequest {
    }
}