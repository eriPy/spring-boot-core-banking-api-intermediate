package com.eripy.banking_system.service.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.security.OperationToken;
import com.eripy.banking_system.entities.security.TokenType;
import com.eripy.banking_system.repositories.penalties.OperationTokenRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class SixDigitTokenService {
    private final GetterService getterService;
    private final SecurityService securityService;
    private final EvaluationService evaluationService;
    private final OperationTokenRepository operationTokenRepository;
    private final EmailService emailService;
    private static final Logger logger = LoggerFactory.getLogger(SixDigitTokenService.class);

    public SixDigitTokenService(
        GetterService getterService,
        SecurityService securityService,
        EvaluationService evaluationService,
        OperationTokenRepository operationtpkTokenRepository,
        EmailService emailService
    ) {
        this.getterService = getterService;
        this.securityService = securityService;
        this.evaluationService = evaluationService;
        this.operationTokenRepository = operationtpkTokenRepository;
        this.emailService = emailService;
    }

    public void generateSixDigitToken(Long id, String operation) {
        logger.info("Evaluate the account data");
        Account account = getterService.getAccountFromDatabase(id);
        securityService.evaluateStatus(account);
        String token = evaluationService.createToken();
        TokenType tt = getterService.getTokenType(operation);
        operationTokenRepository.save(new OperationToken(account, token, tt));
        emailService.sendEmail(
            account.getEmail(), 
            "Operation Token", 
            "You have requested a token to process a " + operation + 
            " on your " + account.getAccountType().getAccountType().replace("_", " ") + ".\n" +
            "Your token is: " + token
        );
    }

    public void evaluateSixDigitToken(String token, Long id, String operation) {
        logger.info("Evaluate the account data");
        Account account = getterService.getAccountFromDatabase(id);
        securityService.evaluateStatus(account);
        TokenType tt = getterService.getTokenType(operation);
    }
}
