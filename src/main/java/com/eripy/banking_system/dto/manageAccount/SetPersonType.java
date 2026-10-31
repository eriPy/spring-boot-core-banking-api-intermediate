package com.eripy.banking_system.dto.manageAccount;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SetPersonType(
    @NotBlank(message = "The token is required")
    @Size(max = 6, min = 6, message = "The token must be 6 characters long")
    String token,
    @NotBlank(message = "The person type is required")
    String personType
) {}
