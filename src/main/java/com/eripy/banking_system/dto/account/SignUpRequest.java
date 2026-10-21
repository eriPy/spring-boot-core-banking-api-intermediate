package com.eripy.banking_system.dto.account;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequest(
    @Size(max = 10, min = 10, message = "The ID must be 10 characters long")
    String idCard,
    @NotBlank(message = "The email is required")
    @Email(message = "Invalid email format")
    @Size(max = 50)
    String email,
    @NotBlank(message = "The account type is required in an account")
    String accountType,
    @NotBlank(message = "The account type is required in an account")
    String personType
) {}
