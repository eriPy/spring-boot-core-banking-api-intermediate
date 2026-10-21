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
import com.eripy.banking_system.dto.AccountInformationResponse;
import com.eripy.banking_system.service.account.AccountService;
import com.eripy.banking_system.service.account.SecurityService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController 
@RequestMapping("/api/account")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;
    private final SecurityService securityService;

    public AccountController(
        AccountService accountService,
        SecurityService securityService
    ) {
        this.accountService = accountService;
        this.securityService = securityService;
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
    @PostMapping("/")
    public ResponseEntity<Map<String, String>> generateSixDigitToken() {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(null);
    }

    @Operation(summary = "Change the person type")
    @PatchMapping("/person")
    public void setPersonType() {
        securityService.evaluateSixDigitToken();
    }

    @Operation(summary = "Change the email")
    @PatchMapping("/email")
    public void setEmail() {
        securityService.evaluateSixDigitToken();
    }

    @Operation(summary = "Close an account")
    @PatchMapping("/close")
    public void closeAccount() {
        securityService.evaluateSixDigitToken();
    }
}
