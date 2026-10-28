package com.eripy.banking_system.service.account;

import java.time.LocalDateTime;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.security.OperationToken;
import com.eripy.banking_system.entities.security.TokenType;
import com.eripy.banking_system.event.UserBehaviorAnalyzedEvent;
import com.eripy.banking_system.exceptions.FormatDataException;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.producer.AnalizedProducer;
import com.eripy.banking_system.repositories.penalties.OperationTokenRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class SixDigitTokenService {
    private final GetterService getterService;
    private final SecurityService securityService;
    private final EvaluationService evaluationService;
    private final OperationTokenRepository operationTokenRepository;
    private final EmailService emailService;
    private final AnalizedProducer analizedProducer;
    private static final Logger logger = LoggerFactory.getLogger(SixDigitTokenService.class);

    public SixDigitTokenService(
        GetterService getterService,
        SecurityService securityService,
        EvaluationService evaluationService,
        OperationTokenRepository operationtpkTokenRepository,
        EmailService emailService,
        AnalizedProducer analizedProducer
    ) {
        this.getterService = getterService;
        this.securityService = securityService;
        this.evaluationService = evaluationService;
        this.operationTokenRepository = operationtpkTokenRepository;
        this.emailService = emailService;
        this.analizedProducer = analizedProducer;
    }

    public void generateSixDigitToken(Long id, String operation) {
        logger.info("Evaluate the account data");
        Account account = securityService.evaluateIfPass(id);
        List<String> invalid = List.of("withdrawal", "deposit", "transfer");
        evaluationService.evaluateAccountWithdrawal(account, operation);
        evaluationService.evaluateFixedDepositDeposit(account, operation, invalid);
        analizedProducer.userAnalized(new UserBehaviorAnalyzedEvent(account));
        String token = evaluationService.createToken();
        TokenType tt = getterService.getTokenType(operation);
        operationTokenRepository.save(new OperationToken(account, token, tt));
        emailService.sendEmail(
            account.getUser().getEmail(), 
            "Operation Token", 
            "You have requested a token to process a " + operation + 
            " on your " + account.getAccountType().getAccountType().replace("_", " ") + ".\n" +
            "Your token is: " + token
        );
    }

    public void evaluateSixDigitToken(String token, Long id, String operation) {
        logger.info("Evaluate the account data");
        Account account = securityService.evaluateIfPass(id);
        TokenType tt = getterService.getTokenType(operation);
        OperationToken ot = operationTokenRepository.findByLastestToken(
            token, 
            account.getAccountType().getAccountType(), 
            account, 
            tt)
            .orElseThrow(() -> {
                logger.warn("Token not found on database");
                throw new ResourceNotFoundException("Token not found");
            });
        if (ot.getIsUsed() || ot.getCreatedAt().isBefore(LocalDateTime.now().minusMinutes(5))) {
            logger.warn(ot.getIsUsed() ? "The token already was used" : "Expired token");
            throw new FormatDataException("Invalid data");
        }
        ot.setIsUsed(true);
        operationTokenRepository.save(ot);
    }
}
