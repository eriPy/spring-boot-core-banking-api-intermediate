package com.eripy.banking_system.dto.manageAccount;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetEmail(
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must be 6 characters long")
    String token,
    @Email(message = "Invalid email format")
    @NotBlank(message = "The email is required")
    @Size(max = 254, message = "The email cannot be more than 254 characters long")
    String email
) {}
