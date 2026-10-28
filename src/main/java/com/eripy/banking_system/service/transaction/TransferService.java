package com.eripy.banking_system.service.transaction;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class TransferService {
    @Transactional 
    public void processTransfer() {
        
    }
}
