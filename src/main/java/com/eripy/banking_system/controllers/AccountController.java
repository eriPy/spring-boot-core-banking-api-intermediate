package com.eripy.banking_system.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.dto.transactions.OperationRequest;
import com.eripy.banking_system.service.account.AccountService;
import com.eripy.banking_system.service.account.SixDigitTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController 
@RequestMapping("/api/account")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;
    private final SixDigitTokenService sixDigitTokenService;

    public AccountController(
        AccountService accountService,
        SixDigitTokenService sixDigitTokenService
    ) {
        this.accountService = accountService;
        this.sixDigitTokenService = sixDigitTokenService;
    }

    @Operation(summary = "Request token and return the user information")
    @GetMapping("/me")
    public AccountInformationResponse getMyInfo(
        Authentication authentication
    ) {
        Long id = Long.parseLong(authentication.getName());
        return accountService.getUserInformation(id);
    }

    @Operation(summary = "Request user information and generate a six digit token to process a sensitive operation")
    @PostMapping("/token")
    public ResponseEntity<Map<String, String>> generateSixDigitToken(
        Authentication authentication,
        OperationRequest request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.generateSixDigitToken(id, request.operation());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(null);
    }

    @Operation(summary = "Change the person type")
    @PatchMapping("/person")
    public void setPersonType() {
        sixDigitTokenService.evaluateSixDigitToken(null, null, "person");
    }

    @Operation(summary = "Change the email")
    @PatchMapping("/email")
    public void setEmail() {
        sixDigitTokenService.evaluateSixDigitToken(null, null, "account");
    }

    @Operation(summary = "Close an account")
    @PatchMapping("/close")
    public void closeAccount() {
        sixDigitTokenService.evaluateSixDigitToken(null, null, "close");
    }
}
