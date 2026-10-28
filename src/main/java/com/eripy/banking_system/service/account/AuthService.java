package com.eripy.banking_system.service.account;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.account.CreateAccoutRequest;
import com.eripy.banking_system.dto.account.SignInRequest;
import com.eripy.banking_system.dto.account.SignUpRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.PayrollAccount;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.exceptions.FormatDataException;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.InvalidDateException;
import com.eripy.banking_system.exceptions.InvalidQuantity;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.UserRepository;
import com.eripy.banking_system.service.GetterService;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service 
public class AuthService {
    private final EvaluationService evaluationService;
    private final EvaluateFormatService evaluateFormatService;
    private final GetterService getterService;
    private final EmailService emailService;
    private final ObjectMapper mapper;
    private final RedisTemplate<String, String> redis;
    private final PasswordEncoder encoder;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final SecretKey secretKey;
    private final SecurityService securityService;
    private final AccountService accountService;
    private final UserRepository userRepository;
    private final RecordService recordService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(
        EvaluationService evaluationService,
        EvaluateFormatService evaluateFormatService,
        ObjectMapper mapper,
        RedisTemplate<String, String> redis,
        EmailService emailService,
        GetterService getterService,
        PasswordEncoder encoder,
        AccountRepository accountRepository,
        JwtService jwtService,
        SecretKey secretKey,
        SecurityService securityService,
        AccountService accountService,
        UserRepository userRepository,
        RecordService recordService
    ) {
        this.evaluationService = evaluationService;
        this.evaluateFormatService = evaluateFormatService;
        this.mapper = mapper;
        this.redis = redis;
        this.emailService = emailService;
        this.getterService = getterService;
        this.encoder = encoder;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
        this.secretKey = secretKey;
        this.securityService = securityService;
        this.accountService = accountService;
        this.userRepository = userRepository;
        this.recordService = recordService;
    }

    public void evaluateData(SignUpRequest request) {
        logger.info("Evaluate data request");
        if (userRepository.existsByEmailOrIdCard(request.email(), request.idCard())) {
            logger.warn("This user exists");
            throw new InconsistenceDataException("Invalid data");
        }
        List<AccountsType> ats = getterService.getAccountTypes();
        List<PersonType> pts = getterService.getPersonType();
        evaluateFormatService.evaluateGeneralData(ats, pts,
            evaluationService.evaluateIdCard(request.idCard()),
            evaluateFormatService.evaluateAccountType(request.accountType(), ats),
            evaluateFormatService.evaluatePersonType(request.personType(), pts)
        );
        securityService.checkDataConsistency(request);
        String token = evaluateFormatService.generateEmailToken();
        emailService.sendEmailToSignUp(request.email(), token);
        try {
            String json = mapper.writeValueAsString(request);
            redis.opsForValue().set("user-data-" + token, json, Duration.ofMinutes(15));

        } catch (JacksonException e) {
            logger.warn("Failed to serialize request to JSON: ", e);
            throw new FormatDataException("Invalid data");
        }
    }
    
    @Transactional 
    public String createAccount(CreateAccoutRequest request) {
        logger.info("Searching user information in redis");
        String data = redis.opsForValue().get("user-data-"+request.token());
        if (data == null) {
            logger.warn("The user information not found in redis");
            throw new ResourceNotFoundException("User information not found");
        }
        logger.info("Information found");
        SignUpRequest userData = mapper.readValue(data, new TypeReference<SignUpRequest>() {});
        if (userRepository.existsByEmailOrIdCard(userData.email(), userData.idCard())) {
            logger.warn("This user exists");
            throw new InconsistenceDataException("Invalid data");
        }
        AccountsType accountType = getterService.getAccountTypeFromCache(userData.accountType());
        PersonType personType = getterService.getPersonTypeFromCache(userData.personType());
        if (accountType.getAccountType().equals("fixed_deposit_account")) {
            logger.info("Validating the date");
            if (request.madureTime() == null) {
                logger.warn("The account have not madure time");
                throw new InvalidDateException("The madure time for a fixed deposit account is required");
            }
            if (request.madureTime().isBefore(LocalDateTime.now().plusMonths(1))) {
                logger.warn("maturation date too early");
                throw new InvalidDateException("Invalid date");
            }
            logger.info("Validated date");
        }
        logger.info("User information complete");
        logger.info("Compare monthly earnings to cap");
        if (request.monthlyEarnings().compareTo(personType.getSalaryCap()) > 0) {
            logger.warn("The balance is greather than the cap");
            throw new InvalidQuantity("Invalid balance");
        }
        String password = encoder.encode(request.password());
        logger.info("Password hashed");
        StringBuilder sb = new StringBuilder("67");
        evaluationService.generateAccountCard(sb);
        Long accountId = recordService.registerUser(request, accountType, userData, password, personType, sb.toString());
        return jwtService.createJwt(
                accountId, 
                jwtExpiration, 
                secretKey
            );
    }

    public String login(SignInRequest request) {
        logger.info("Validating data");
        if (!accountRepository.existsByEmail(request.email())) {
            logger.warn("The email not found on database");
            throw new FormatDataException("Invalid data");
        }
        List<Account> data = getterService.getInformation(request.email());
        for (Account account: data) accountService.unfreeze(account);
        String password = data.get(0).getPassword();
        if (!encoder.matches(request.password(), password)) {
            logger.warn("The password not match");
            throw new FormatDataException("Invalid data");
        }
        return jwtService.createJwt(
            data.get(0).getId(), 
            jwtExpiration, 
            secretKey
        );
    }
}
