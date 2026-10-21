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
import com.eripy.banking_system.service.transaction.DepositService;
import com.eripy.banking_system.service.transaction.WithdrawalService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/transaction")
@SecurityRequirement(name = "bearerAuth")
public class TransactionController {
    private final DepositService depositService;
    private final WithdrawalService withdrawalService;

    public TransactionController(
        DepositService depositService,
        WithdrawalService withdrawalService
    ) {
        this.depositService = depositService;
        this.withdrawalService = withdrawalService;
    }

    @PostMapping("/deposit")
    public ResponseEntity<Map<String, String>> deposit(
        Authentication authentication,
        @Valid 
        @RequestBody
        DepositRequest request
    ) {
        Long id = Long.parseLong(authentication.getName());
        depositService.processDeposit(id, request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("Deposit", "succesful")
        );
    }

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
    public void transfer() {

    }

    @PostMapping("/user/interest-charge")
    public void interestCharge() {
        
    }
}
