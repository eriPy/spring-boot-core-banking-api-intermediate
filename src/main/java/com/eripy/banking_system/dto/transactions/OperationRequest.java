package com.eripy.banking_system.dto.transactions;

import java.util.List;
import com.eripy.banking_system.exceptions.FormatDataException;
import jakarta.validation.constraints.NotBlank;

public record OperationRequest(
    @NotBlank 
    String operation
) {
    public OperationRequest {
        List<String> valid = List.of("deposit", "withdrawal", "transfer", "person", "earning", "account", "close");
        if (operation == null || !valid.contains(operation)) {
            throw new FormatDataException("Invalid data");
        }
    }
}
