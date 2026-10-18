package com.eripy.banking_system.service.account;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import javax.crypto.SecretKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.CreateAccountResponse;
import com.eripy.banking_system.dto.CreateAccoutRequest;
import com.eripy.banking_system.dto.SignInRequest;
import com.eripy.banking_system.dto.SignUpRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.PayrollAccount;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.exceptions.FormatDataException;
import com.eripy.banking_system.exceptions.InvalidDateException;
import com.eripy.banking_system.exceptions.InvalidQuantity;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.accounts.FixedDepositAccountRepository;
import com.eripy.banking_system.repositories.accounts.PayrollAccountRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
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
    private final SecureRandom random;
    private final PasswordEncoder encoder;
    private final AccountRepository accountRepository;
    private final JwtService jwtService;
    private final SecretKey secretKey;
    private final CheckingAccountRepository checkingAccountRepository;
    private final FixedDepositAccountRepository fixedDepositAccountRepository;
    private final PayrollAccountRepository payrollAccountRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final SecurityService securityService;
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    public AuthService(
        EvaluationService evaluationService,
        EvaluateFormatService evaluateFormatService,
        ObjectMapper mapper,
        RedisTemplate<String, String> redis,
        SecureRandom random,
        EmailService emailService,
        GetterService getterService,
        PasswordEncoder encoder,
        AccountRepository accountRepository,
        JwtService jwtService,
        SecretKey secretKey,
        CheckingAccountRepository checkingAccountRepository,
        FixedDepositAccountRepository fixedDepositAccountRepository,
        PayrollAccountRepository payrollAccountRepository,
        SavingsAccountRepository savingsAccountRepository,
        SecurityService securityService
    ) {
        this.evaluationService = evaluationService;
        this.evaluateFormatService = evaluateFormatService;
        this.mapper = mapper;
        this.redis = redis;
        this.random = random;
        this.emailService = emailService;
        this.getterService = getterService;
        this.encoder = encoder;
        this.accountRepository = accountRepository;
        this.jwtService = jwtService;
        this.secretKey = secretKey;
        this.checkingAccountRepository = checkingAccountRepository;
        this.fixedDepositAccountRepository = fixedDepositAccountRepository;
        this.payrollAccountRepository = payrollAccountRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.securityService = securityService;
    }

    public void evaluateData(SignUpRequest request) {
        logger.info("Evaluate data request");
        List<AccountsType> ats = getterService.getAccountTypes();
        List<PersonType> pts = getterService.getPersonType();
        if (!evaluationService.evaluateIdCard(request.idCard())) {
            logger.warn("The id card is invalid");
            throw new FormatDataException("Invalid data");
        }
        if (!evaluateFormatService.evaluateAccountType(request.accountType(), ats)) {
            logger.warn("The account type has no a valid formar");
            throw new FormatDataException("Invalid data");
        }
        if (!evaluateFormatService.evaluatePersonType(request.personType(), pts)) {
            logger.warn("The person type has no a valid formar");
            throw new FormatDataException("Invalid data");
        }
        AccountsType at = evaluateFormatService.getAccountTypeObject(request.accountType(), ats);
        PersonType student = evaluateFormatService.getPersonTypeObject("student", pts);
        PersonType unemployed = evaluateFormatService.getPersonTypeObject("unemployed", pts);
        securityService.checkDataConsistency(request, at, student, unemployed);
        String token = generateEmailToken();
        emailService.sendEmailToSignUp(request.email(), token);
        try {
            String json = mapper.writeValueAsString(request);
            redis.opsForValue().set("user-data-" + token, json, Duration.ofMinutes(15));

        } catch (JacksonException e) {
            logger.warn("Failed to serialize request to JSON: ", e);
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
    
    @Transactional 
    public CreateAccountResponse createAccount(CreateAccoutRequest request) {
        logger.info("Searching user information in redis");
        String data = redis.opsForValue().get("user-data-"+request.token());
        if (data == null) {
            logger.warn("The user information not found in redis");
            throw new ResourceNotFoundException("User information not found");
        }
        List<AccountsType> accountsTypes = getterService.getAccountTypes();
        AccountsType accountType = null;
        PersonType personType = null;
        logger.info("Information found");
        SignUpRequest userData = mapper.readValue(data, new TypeReference<SignUpRequest>() {});
        List<PersonType> pt = getterService.getPersonType();
        for (PersonType person: pt) {
            if (person.getPersonType().equals(userData.personType())) {
                personType = person;
                break;
            }
        }
        for (AccountsType act: accountsTypes) {
            if (act.getAccountType().equals(userData.accountType())) {
                accountType = act;
                break;
            }
        }
        if (personType == null || accountType == null) {
            logger.warn("Something went wrong in types");
            throw new ResourceNotFoundException("Type Error");
        }
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
        Account account = new Account(
            request.accountHolder(),
            userData.idCard(),
            userData.email(),
            password,
            accountType,
            sb.toString(),
            personType,
            request.monthlyEarnings()
        );
        logger.info("Account information created");
        accountRepository.save(account);
        logger.info("Saving the account type");
        switch (userData.accountType()) {
            case "savings_account":
                savingsAccountRepository.save(new SavingsAccount(account));
                break;
            case "checking_account":
                checkingAccountRepository.save(new CheckingAccount(account));
                break;
            case "payroll_account":
                payrollAccountRepository.save(new PayrollAccount(account)); 
                break;
            case "fixed_deposit_account":
                fixedDepositAccountRepository.save(new FixedDepositAccount(account, request.madureTime()));
                break;
            default:
                logger.warn("The account type is invalid");
                throw new FormatDataException("Error type");
        }
        logger.info("The account was created");
        return new CreateAccountResponse(
            jwtService.createJwt(
                account.getId(), 
                jwtExpiration, 
                secretKey
            ),
            account.getId()
        );
    }

    public String login(SignInRequest request) {
        logger.info("Validating data");
        if (!accountRepository.existsByEmail(request.email())) {
            logger.warn("The email not found on database");
            throw new FormatDataException("Invalid data");
        }
        List<Account> data = getterService.getInformation(request.email());
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
