package com.eripy.banking_system.dto.manageAccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record Password(
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must be 6 characters long")
    String token,
    @NotBlank(message = "The password is required")
    @Size(min = 8, message = "The password must be at least 8 characters long")
    String password
) {}
