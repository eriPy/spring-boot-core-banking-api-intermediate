package com.eripy.banking_system.dto.account;

import java.time.LocalDateTime;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;

public record CreateNewAccountRequest(
    @NotBlank 
    String token,
    @Future(message = "The data must be in the future")
    LocalDateTime madureTime,
    @NotBlank(message = "The account type is required in an account")
    String accountType
) {
    public CreateNewAccountRequest {
        if (accountType.equals("fixed_deposit_account") && madureTime == null) {
            throw new InconsistenceDataException("The maturity date for fixed deposits accounts is required");
        }
    }
}
