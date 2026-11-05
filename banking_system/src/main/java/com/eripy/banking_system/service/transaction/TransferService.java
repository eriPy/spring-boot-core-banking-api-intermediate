package com.eripy.banking_system.service.transaction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service 
public class TransferService {
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    @Transactional 
    public void processTransfer() {
        logger.info("Processing the transfer");
    }
}
