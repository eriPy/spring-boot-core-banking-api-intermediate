package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;
import java.util.List;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record TransferRequest(
    @NotNull (message = "The amount is required")
    @DecimalMin(value = "0.01", message = "The deposit must be least 0.01")
    BigDecimal amount,
    @NotBlank(message = "The token is required")
    @Size(min = 6, max = 6, message = "The token must has 6 characters long")
    String token,
    @NotBlank(message = "The payment is required")
    String paymentMethod,
    @NotBlank(message = "The account type is required")
    String accountType,
    @NotBlank(message = "The account card to transfer is required")
    @Size(min = 12, max = 12, message = "The account card must have 12 characters")
    String accountCardToTransfer
) {
    public TransferRequest {
        if (accountType.equals("fixed_deposit_account")) {
            throw new InconsistenceDataException("Invalid data request");
        }
        List<String> valids = List.of("bank_teller", "bank", "atm");
        if (!valids.contains(paymentMethod)) {
            throw new InconsistenceDataException("Invalid data request");
        }
    }   
} 