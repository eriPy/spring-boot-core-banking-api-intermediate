package com.eripy.banking_system.service.account;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.User;
import com.eripy.banking_system.exceptions.FormatDataException;

@Service 
public class EvaluateFormatService {
    private final SecureRandom random;
    private static final Logger logger = LoggerFactory.getLogger(EvaluateFormatService.class);

    public EvaluateFormatService(SecureRandom random) {
        this.random = random;
    }

    public boolean evaluateAccountType(String accountType, List<AccountsType> types) {
        logger.info("Fetching data format");
        return types.stream()
             .anyMatch(type -> type.getAccountType().equals(accountType));
    }

    public boolean evaluatePersonType(String personType, List<PersonType> types) {
        logger.info("Fetching data format");
        return types.stream()
            .anyMatch(type -> type.getPersonType().equals(personType));
    }

    public AccountInformationResponse getInformationFormat(User user, List<Account> accounts) {
        List<Map<Long, String>> information = new ArrayList<>();
        for (Account account: accounts) {
            information.add(Map.of(account.getId(), account.getAccountType().getAccountType()));
        }
        String uncensoredIdCard = user.getIdCard();
        String censoredIdCard = "********" + uncensoredIdCard.substring(uncensoredIdCard.length() - 2);
        return new AccountInformationResponse(
            user.getAccountHolder(),
            user.getEmail(),
            censoredIdCard,
            user.getPersonType().getPersonType(),
            user.getMonthlyEarnings(),
            information
        );
    }

    public void evaluateGeneralData(
        List<AccountsType> ats,
        List<PersonType> pts,
        boolean invalidIdCard,
        boolean invalidAccountType,
        boolean invalidPersonType
    ) {
        if (!invalidIdCard) {
            logger.warn("The id card is invalid");
            throw new FormatDataException("Invalid data");
        }
        if (!invalidAccountType) {
            logger.warn("The account type has no a valid formar");
            throw new FormatDataException("Invalid data");
        }
        if (!invalidPersonType) {
            logger.warn("The person type has no a valid formar");
            throw new FormatDataException("Invalid data");
        }   
    }

    public String generateEmailToken() {
        byte[] bytes = new byte[32];
        random.nextBytes(bytes);
        return Base64.getUrlEncoder()
            .withoutPadding()
            .encodeToString(bytes);
    }
}