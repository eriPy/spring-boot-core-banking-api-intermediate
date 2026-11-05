package com.eripy.banking_system.controllers;

import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.function.EntityResponse;
import com.eripy.banking_system.dto.account.CreateAccoutRequest;
import com.eripy.banking_system.dto.account.SignInRequest;
import com.eripy.banking_system.dto.account.SignUpRequest;
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

    @Operation(summary = """
        For new users
        Evaluate the information and send an email using an appropriate tone        
    """)
    @PostMapping("/register")
    public Map<String, String> sendEmailToken(
        @Valid 
        @RequestBody  
        SignUpRequest request
    ) {
        authService.evaluateData(request);
        return Map.of("res", "A verification email has been sent");
    }    

    @Operation(summary = "Use the data saved in sendEmailToken and create the account and return a JWT")
    @PostMapping("/sign-up")
    public ResponseEntity<Map<String, String>> singUp(
        @Valid 
        @RequestBody 
        CreateAccoutRequest request
    ) {
        String token = authService.createAccount(request);
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of(
                "res", "Account was create", 
                "token", token
            ));
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

    @Operation(summary = """
        For registered users
        This endpoint receives the auth information of the user and valids if is correct to generate a token to
        create other account        
    """)
    @PostMapping("/user")
    public EntityResponse<Map<String, Object>> isUser() {
        return null;
    }

    @Operation(summary = "This endpoint receives the new account information of one user and create the account")
    @PostMapping("/new-account")
    public EntityResponse<Map<String, Object>> newAccount() {
        return null;
    }
}
