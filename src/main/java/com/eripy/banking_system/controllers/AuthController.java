package com.eripy.banking_system.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.CreateAccountResponse;
import com.eripy.banking_system.dto.CreateAccoutRequest;
import com.eripy.banking_system.dto.SignInRequest;
import com.eripy.banking_system.dto.SignUpRequest;
import com.eripy.banking_system.service.account.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;

@RestController 
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(
        AuthService authService
    ) {
        this.authService = authService;
    }

    @Operation(summary = "Evaluate the information and send an email using an appropriate tone")
    @PostMapping("/send-verification-email")
    public Map<String, String> sendEmailToken(
        @Valid 
        @RequestBody  
        SignUpRequest request
    ) {
        authService.evaluateData(request);
        return Map.of("res", "A verification email has been sent");
    }    

    @Operation(summary = "Enter a email and a password to sign in and a JWT")
    @PostMapping("/sign-in")
    public Map<String, Object> signIn(
        @Valid 
        @RequestBody 
        SignInRequest request
    ) {
        String token = authService.login(request);
        return Map.of(
            "data", Map.of(
            "token", token
        ));
    }

    @Operation(summary = "Use the data saved in sendEmailToken and create the account and return a JWT")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, Object>> singUp(
        @Valid 
        @RequestBody 
        CreateAccoutRequest request
    ) {
        CreateAccountResponse response = authService.createAccount(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of(
                "res", "Account was create", 
                "data", Map.of(
                    "token", response.token(),
                    "id", response.id()
                )
            ));
    }

    @Operation(summary = "Confirm the password if user has various accounts")
    @PostMapping("/password")
    public void confirm() {

    }
}
