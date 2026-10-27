package com.eripy.banking_system.service;

import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.eripy.banking_system.dto.account.IdEntity;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.entities.security.AccountPenaltiesByFlagged;
import com.eripy.banking_system.entities.security.History;
import com.eripy.banking_system.entities.security.Overdraft;
import com.eripy.banking_system.entities.security.PenalizedReason;
import com.eripy.banking_system.entities.security.TokenType;
import com.eripy.banking_system.entities.transactions.PaymentMethod;
import com.eripy.banking_system.entities.transactions.TransactionType;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.AccountsTypeRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.accounts.FixedDepositAccountRepository;
import com.eripy.banking_system.repositories.accounts.PersonTypeRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
import com.eripy.banking_system.repositories.penalties.AccountPenaltiesByFlaggedRepository;
import com.eripy.banking_system.repositories.penalties.HistoryRepository;
import com.eripy.banking_system.repositories.penalties.OverdraftRepository;
import com.eripy.banking_system.repositories.penalties.PenalizedReasonRepository;
import com.eripy.banking_system.repositories.penalties.TokenTypeRepository;
import com.eripy.banking_system.repositories.transactions.PaymentMethodRepository;
import com.eripy.banking_system.repositories.transactions.TransactionTypeRepository;
import tools.jackson.core.JacksonException;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

@Service  
public class GetterService {
    private final RedisTemplate<String, String> redis;
    private final ObjectMapper mapper;
    private final AccountsTypeRepository accountsTypeRepository;
    private final PersonTypeRepository personTypeRepository;
    private final AccountRepository accountRepository;
    private final TransactionTypeRepository transactionTypeRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final FixedDepositAccountRepository fixedDepositAccountRepository;
    private final CheckingAccountRepository checkingAccountRepository;
    private final OverdraftRepository overdraftRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final TokenTypeRepository tokenTypeRepository;
    private final PenalizedReasonRepository penalizedReasonRepository;
    private final HistoryRepository historyRepository;
    private final AccountPenaltiesByFlaggedRepository accountPenaltiesByFlaggedRepository;
    private static final Logger logger = LoggerFactory.getLogger(GetterService.class);

    public GetterService(
        RedisTemplate<String, String> redis,
        ObjectMapper mapper,
        AccountsTypeRepository accountsTypeRepository,
        PersonTypeRepository personTypeRepository,
        AccountRepository accountRepository,
        TransactionTypeRepository transactionTypeRepository,
        SavingsAccountRepository savingsAccountRepository,
        FixedDepositAccountRepository fixedDepositAccountRepository,
        CheckingAccountRepository checkingAccountRepository,
        OverdraftRepository overdraftRepository,
        PaymentMethodRepository paymentMethodRepository,
        TokenTypeRepository tokenTypeRepository,
        PenalizedReasonRepository penalizedReasonRepository,
        HistoryRepository historyRepository,
        AccountPenaltiesByFlaggedRepository accountPenaltiesByFlaggedRepository
    ) {
        this.redis = redis;
        this.mapper = mapper;
        this.accountsTypeRepository = accountsTypeRepository;
        this.personTypeRepository = personTypeRepository;
        this.accountRepository = accountRepository;
        this.transactionTypeRepository = transactionTypeRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.fixedDepositAccountRepository = fixedDepositAccountRepository;
        this.checkingAccountRepository = checkingAccountRepository;
        this.overdraftRepository = overdraftRepository;
        this.paymentMethodRepository = paymentMethodRepository;
        this.tokenTypeRepository = tokenTypeRepository;
        this.penalizedReasonRepository = penalizedReasonRepository;
        this.historyRepository = historyRepository;
        this.accountPenaltiesByFlaggedRepository = accountPenaltiesByFlaggedRepository;
    }

    public <T> void saveToRedis(String key, T data, Duration duration) throws JacksonException {
        redis.opsForValue().set(key, mapper.writeValueAsString(data), duration);
    }

    private <T> T getFromCache(
        String Key,
        String value, 
        TypeReference<T> data,
        Supplier<T> databaseFetcher
    ) throws JacksonException {
        try {
            logger.info("Search" + value + " in Redis");
            String cache = redis.opsForValue().get(Key);
            if (cache == null) {
                logger.info(value + " not found in Redis");
                T db = databaseFetcher.get();
                logger.info(value + " found on database");
                saveToRedis(Key, db, Duration.ofHours(12));
                logger.info(value + " saved in Redis");
                return db;
            }
            logger.info(value + " found in Redis");
            return mapper.readValue(cache, data); 
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            return databaseFetcher.get();
        } catch (JacksonException e) {
            logger.warn("Data in Redis does not have a valid format: " + e.getMessage());
            return databaseFetcher.get();
        }
    }

    private <T> T getFromDatabase(Supplier<Optional<T>> databaseFetcher, String value) {
        return databaseFetcher.get()
            .orElseThrow(() -> {
                logger.warn(value + " not found on database");
                throw new ResourceNotFoundException("Not found");});
    }

    private PenalizedReason getPenalizedReasonFromDatabase(String reason) {
        return getFromDatabase(() -> penalizedReasonRepository.findByReason(reason), "Reason");
    }

    public PenalizedReason getPenalizedReason(String reason) {
        return getFromCache(
            "penalized-reason-" + reason, 
            "Reason", 
            new TypeReference<PenalizedReason>() {}, 
            () -> getPenalizedReasonFromDatabase(reason));
    }

    public List<AccountsType> getAccountTypes() {
        return getFromCache(
            "account-types", 
            "Account types", 
            new TypeReference<List<AccountsType>>() {}, 
            () -> accountsTypeRepository.findAll());
    }

    public List<PersonType> getPersonType() {
        return getFromCache(
            "person-types", 
            "Person Types", 
            new TypeReference<List<PersonType>>() {}, 
            () -> personTypeRepository.findAll());
    }

    private TransactionType getTransactionTypeFromDatabase(String type) {
        return getFromDatabase(() -> transactionTypeRepository.findByTransactionType(type), "Transaction");
    }

    public TransactionType getTransactionType(String type) {
        return getFromCache(
            type + "-transactions-object", 
            "Transaction type", 
            new TypeReference<TransactionType>() {}, 
            () -> getTransactionTypeFromDatabase(type));
    }    

    public List<Account> getInformation(String email) {
        return getFromCache(
            "user-" + email + "-session",
            "User information",
            new TypeReference<List<Account>>() {}, 
            () -> accountRepository.findByEmail(email));
    }

    public Account getAccountFromDatabase(Long id) {
        return getFromDatabase(() -> accountRepository.findById(id), "Account");
    }

    public SavingsAccount getSavingsAccountFromDatabase(Long id) {
        return getFromDatabase(() -> savingsAccountRepository.findById(id), "Saving account");
    }

    public FixedDepositAccount getFixedDepositAccountFromDatabase(Long id) {
        return getFromDatabase(() -> fixedDepositAccountRepository.findById(id), "Fixed deposit account ");
    }

    public CheckingAccount getCheckingAccountFromDatabase(Long id) {
        return getFromDatabase(() -> checkingAccountRepository.findById(id), "Checking account");
    }

    private BigDecimal getExcessTransactionFeeFromDatabase(Long id) {
        return getFromDatabase(() -> savingsAccountRepository.findById(id), "Fee ").getExcessTransactionFee();
    }

    public BigDecimal getExcessTransactionFee(Long id) {
        return getFromCache(
            "excess-transactions-fee", 
            "Excess transaction fee", 
            new TypeReference<BigDecimal>() {}, 
            () -> getExcessTransactionFeeFromDatabase(id));
    }

    private Overdraft getOverdraftFromDatabase(PersonType personType) {
        return getFromDatabase(() -> overdraftRepository.findByPersonType(personType), "overdraft");
    }  

    public Overdraft getOverdraft(PersonType personType) {
        return getFromCache("overdraft-for-" + personType.getPersonType(),
            "Overdraft", 
            new TypeReference<Overdraft>() {}, 
            () -> getOverdraftFromDatabase(personType));
    }

    private PaymentMethod getPaymentMethodFromDatabase(String paymentMethod) {
        return getFromDatabase(() -> paymentMethodRepository.findByPaymentMethod(paymentMethod), "Payment method");
    }

    public PaymentMethod getPaymentMethod(String paymentMethod) {
        return getFromCache("payment-method" + paymentMethod, 
        "Payment method", 
            new TypeReference<PaymentMethod>() {}, 
            () -> getPaymentMethodFromDatabase(paymentMethod));
    }

    private TokenType getTokenTypeFromDatabase(String tokentType) {
        return getFromDatabase(() -> tokenTypeRepository.findByTokenType(tokentType), "Token type");
    }

    public TokenType getTokenType(String tokenType) {
        return getFromCache(
            "token-type-" + tokenType, 
            "Token " + tokenType, 
            new TypeReference<TokenType>() {}, 
            () -> getTokenTypeFromDatabase(tokenType));
    }

    private <T extends IdEntity> T getOrCreate(
        Supplier<Optional<T>> dataFecher,
        Supplier<T> constructor,
        Function<T, T> save,
        String msg
    ) {
        T t = dataFecher.get()
            .orElseGet(() -> {
                logger.info("The user has no " + msg);
                return constructor.get();
            });
        if (t.getId() == null) return save.apply(t);
        return t;
    }

    public History getOrCreateHistory(Account account, String msg) {
        return getOrCreate(
            () -> historyRepository.findByAccount(account), 
            () -> new History(account),
            (history) -> historyRepository.save(history), 
            msg
        );
    }

    public AccountPenaltiesByFlagged getOrCreateAccountPenaltiesByFlagged(History history, String msg) {
        return getOrCreate(
            () -> accountPenaltiesByFlaggedRepository.findByHistory(history), 
            () -> new AccountPenaltiesByFlagged(history), 
            (apf) -> accountPenaltiesByFlaggedRepository.save(apf),
            msg
        );
    }
}