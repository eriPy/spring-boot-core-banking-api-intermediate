package com.eripy.banking_system.service.account;

import java.security.SecureRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.repositories.accounts.AccountRepository;

@Service 
public class EvaluationService {
    private final AccountRepository accountRepository;
    private final SecureRandom random;
    private static final Logger logger = LoggerFactory.getLogger(EvaluationService.class);

    public EvaluationService(
        AccountRepository accountRepository,
        SecureRandom random
    ) {
        this.accountRepository = accountRepository;
        this.random = random;
    }

    public boolean evaluateIdCard(String idCard) {
        int total = 0;
        int startIn = 9;
        for (int i = 0; i < idCard.length(); i++) {
            if ((i == 8 && idCard.charAt(8) == '-')) continue;
            if (i != 8 && Character.isDigit(idCard.charAt(i))) {
                if (i == 9) continue;
                int value = idCard.charAt(i) - '0';
                total += value * startIn;
                startIn--;
                continue;
            }
            return false;
        }
        int lastDigit = idCard.charAt(9) - '0';
        int residue = total % 10;
        return (residue == 0 && residue == lastDigit) || Math.abs(10 - residue) == lastDigit;
    }

    public void generateAccountCard(StringBuilder currentCard) {
        logger.info("Loop started");
        while (true) {
            for (int i = 0; i < 9; i++) {
                currentCard.append(random.nextInt(10));
            }
            logger.info("Digits generated");
            int totalDouble = 0;
            int totalNormal = 0;
            boolean turn = false;
            for (int i = currentCard.length() - 1; i >= 0; i--) {
                if (turn) {
                    totalNormal += currentCard.charAt(i) - '0';
                    turn = false;
                } else {
                    int value = (currentCard.charAt(i) - '0') * 2;
                    totalDouble += (value % 10) + (value / 10);
                    turn = true;
                }   
            }
            currentCard.append((10 - ((totalDouble + totalNormal) % 10)) % 10);
            logger.info("Last digit generated");
            if (!accountRepository.existsByAccountCard(currentCard.toString())) {
                logger.info("Account card created");
                break;
            }
            currentCard.delete(2, currentCard.length());
            logger.warn("The account card already exists");
        }
    }

    public String createToken() {
        logger.info("Generating token");
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            sb.append(chars.charAt(index));
        }
        logger.info("Token was create");
        return sb.toString();
    }
}
