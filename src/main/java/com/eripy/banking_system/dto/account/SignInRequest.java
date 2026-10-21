package com.eripy.banking_system.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignInRequest(
    @Email(message = "Invalid email format")
    @NotBlank(message = "email is required")
    @Size(max = 254)
    String email,
    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 64, message = "Password must be between 8 and 64 long")
    String password
) {}