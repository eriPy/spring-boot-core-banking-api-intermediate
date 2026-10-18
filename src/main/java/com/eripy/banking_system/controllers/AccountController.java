package com.eripy.banking_system.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.AccountInformationResponse;
import com.eripy.banking_system.service.account.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController 
@RequestMapping("/api/account")
@SecurityRequirement(name = "bearerAuth")
public class AccountController {
    private final AccountService accountService;

    public AccountController(
        AccountService accountService
    ) {
        this.accountService = accountService;
    }

    @Operation(summary = "Request token and return the user information")
    @GetMapping("/me")
    public AccountInformationResponse getMyInfo(
        Authentication authentication
    ) {
        Long id = Long.parseLong(authentication.getName());
        return accountService.getUserInformation(id);
    }

    @Operation(summary = "Change the person type")
    @PatchMapping("/person")
    public void setPersonType() {

    }

    @Operation(summary = "Change the email")
    @PatchMapping("/email")
    public void setEmail() {
        
    }

    @Operation(summary = "Close an account")
    @PatchMapping("/close")
    public void closeAccount() {
        
    }
}
