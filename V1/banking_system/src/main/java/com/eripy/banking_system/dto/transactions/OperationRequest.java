package com.eripy.banking_system.dto.transactions;

import java.util.List;
import com.eripy.banking_system.exceptions.FormatDataException;
import jakarta.validation.constraints.NotBlank;

public record OperationRequest(
    @NotBlank 
    String operation,
    @NotBlank(message = "The account type is required")
    String accountType
) {
    public OperationRequest {
        List<String> valid = List.of(
            "deposit",
            "withdrawal", 
            "transfer", 
            "person", 
            "earnings", 
            "email", 
            "password",
            "close", 
            "holder");
        if (operation == null || !valid.contains(operation)) {
            throw new FormatDataException("Invalid data");
        }
    }
}
