package com.eripy.banking_system.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.transactions.DepositRequest;
import com.eripy.banking_system.dto.transactions.WithdrawalRequest;
import com.eripy.banking_system.service.account.SecurityService;
import com.eripy.banking_system.service.transaction.DepositService;
import com.eripy.banking_system.service.transaction.WithdrawalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/transaction")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final DepositService depositService;
    private final WithdrawalService withdrawalService;
    private final SecurityService securityService;

    public TransactionController(
        DepositService depositService,
        WithdrawalService withdrawalService,
        SecurityService securityService
    ) {
        this.depositService = depositService;
        this.withdrawalService = withdrawalService;
        this.securityService = securityService;
    }

    @Operation(summary = """
        First get the login's jwt, after get a six digit token for a sensitive operation and the controller
        request the amount, payment method and token of the operation and evaluate if all data is valid
        to deposit the amount in account and create the operation on database   
    """)
    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(
        Authentication authentication,
        @Valid 
        @RequestBody
        DepositRequest request
    ) {
        Long id = Long.parseLong(authentication.getName());
        securityService.evaluateSixDigitToken();
        depositService.processDeposit(id, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("Deposit", "succesful")
        );
    }

    @Operation(summary = """
        First get the login's jwt, after get a six digit token for a sensitive operation and the controller
        request the amount, payment method and token of the operation and evaluate if all data is valid
        to withdrawal the amount in account and create the operation on database   
    """)
    @PostMapping("/withdrawal")
    public ResponseEntity<Map<String, String>> withdrawal(
        Authentication authentication,
        WithdrawalRequest request
    ) {

        Long id = Long.parseLong(authentication.getName());
        withdrawalService.processWithdrawal(id, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("Withdrawal", "succesful")
        );
    }

    @PostMapping("/transfer")
    public void transfer(
        Authentication authentication
    ) {
        Long id = Long.parseLong(authentication.getName());
        securityService.evaluateSixDigitToken();
    }
}
