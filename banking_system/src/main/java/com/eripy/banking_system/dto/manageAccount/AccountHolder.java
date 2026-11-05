package com.eripy.banking_system.dto.manageAccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AccountHolder(
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must be 6 characters long")
    String token,
    @NotBlank(message = "The new account holder is required")
    String accountHolder
) {}
