package com.eripy.banking_system.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.dto.manageAccount.AccountHolder;
import com.eripy.banking_system.dto.manageAccount.MonthlyEarnings;
import com.eripy.banking_system.dto.manageAccount.Password;
import com.eripy.banking_system.dto.manageAccount.PersonType;
import com.eripy.banking_system.dto.manageAccount.SetEmail;
import com.eripy.banking_system.dto.transactions.OperationRequest;
import com.eripy.banking_system.service.account.AccountService;
import com.eripy.banking_system.service.account.SixDigitTokenService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;

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
        @Valid  
        @RequestBody 
        OperationRequest request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.generateSixDigitToken(id, request.operation());
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("Success", "true"));
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, set the person type registered in the account
        The person type must be different from the registered person type
    """)
    @PatchMapping("/person")
    public ResponseEntity<Void> setPersonType(
        Authentication authentication,
        @Valid 
        @RequestBody 
        PersonType request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(request.token(), id, "person");
        accountService.setPersonType(request, id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, set the email address registered in the account
        The email address must be different from the registered email address
    """)
    @PatchMapping("/email")
    public ResponseEntity<Void> setEmail(
        Authentication authentication,
        @Valid 
        @RequestBody 
        SetEmail request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(request.token(), id, "email");
        accountService.setEmail(request, id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, set the password registered in the account
        The password must be different from the registered password        
    """)
    @PatchMapping("/password")
    public ResponseEntity<Void> setPassword(
        Authentication authentication,
        @Valid 
        @RequestBody 
        Password request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(request.token(), id, "password");
        accountService.setPassword(request, id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, set the monthly earnings registered in the account
        The monthly earnings must be different from the registered monthly earnings        
    """)
    @PatchMapping("/montly-earnings")
    public ResponseEntity<Void> setMonthlyEarnings(
        Authentication authentication,
        @Valid 
        @RequestBody 
        MonthlyEarnings request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(request.token(), id, "earnings");
        accountService.setMonthlyEarnings(request, id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, set the account holder registered in the account
        The account holder must be different from the registered account holder
    """)
    @PatchMapping("/holder")
    public ResponseEntity<Void> setAccountHolder(
        Authentication authentication,
        @Valid 
        @RequestBody 
        AccountHolder request
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(request.token(), id, "holder");
        accountService.setAccountHolder(request, id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }

    @Operation(summary = """
        First, generate a six-digit token. Then, you can close the account
        If you close the account, it will be disabled and you will no longer be able to use it 
    """)
    @PatchMapping("/close/{token}")
    public ResponseEntity<Void> closeAccount(
        Authentication authentication,
        @PathVariable 
        String token
    ) {
        Long id = Long.parseLong(authentication.getName());
        sixDigitTokenService.evaluateSixDigitToken(token, id, "close");
        accountService.closeAccount(id);
        return ResponseEntity
            .status(HttpStatus.NO_CONTENT)
            .build();
    }
}
