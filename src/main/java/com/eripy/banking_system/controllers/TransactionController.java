package com.eripy.banking_system.controllers;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController 
@RequestMapping("/api/transaction")
public class TransactionController {
    @PostMapping("/deposit")
    private void deposit() {

    }

    @PostMapping("/withdrawal")
    private void withdrawal() {

    }

    @PostMapping("/transfer")
    private void transfer() {

    }

    @PostMapping("/user/interest-charge")
    private void interestCharge() {
        
    }
}
