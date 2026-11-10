package com.eripy.banking_system.service.account;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.RedisSystemException;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.dashboard.UserInformation;
import com.eripy.banking_system.dto.dashboard.UserTransactions;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.User;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.core.type.TypeReference;

@Service 
public class DashboardService {
    private final GetterService getterService;
    private final RedisTemplate<String, String> redis;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final ObjectMapper mapper;
    private final SecurityService securityService;
    private static final Logger logger = LoggerFactory.getLogger(DashboardService.class);

    public DashboardService(
        GetterService getterService,
        RedisTemplate<String, String> redis,
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        ObjectMapper mapper,
        SecurityService securityService
    ) {
        this.getterService = getterService;
        this.redis = redis;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.mapper = mapper;
        this.securityService = securityService;
    }
    
    public boolean userHasDataInRedis(Long id) {return Boolean.TRUE.equals(redis.hasKey("user-" + id + "information"));}

    private UserInformation isNotInRedis(Account account, User user, String key) {
        CheckingAccount checkingAccount = account.getCheckingAccount();
        FixedDepositAccount fixedDepositAccount = account.getFixedDepositAccount();
        logger.info("Data that require a list");
        List<String> accountCards = new ArrayList<>();
        List<String> accountTypes = new ArrayList<>();
        List<BigDecimal> balances = new ArrayList<>();
        List<Account> accounts = accountRepository.findByUser(user);
        logger.info("Redacting account details");
        for (Account a: accounts) {
            accountTypes.add(a.getAccountType().getAccountType());
            String current = a.getAccountCard();
            accountCards.add("********" + current.substring(current.length() - 4));
            balances.add(a.getBalance());
        }
        List<UserTransactions> userTransactions = getMyTransactions(accounts);
        BigDecimal overdraftLimit = checkingAccount == null ? null : checkingAccount.getOverdraftLimit();
        BigDecimal overdraftUsed = checkingAccount == null ? null : checkingAccount.getOverdraftUsed();
        BigDecimal interestPayable = checkingAccount == null ? null : checkingAccount.getInterestPayable();
        LocalDateTime maturityDate = fixedDepositAccount == null ? null : fixedDepositAccount.getMaturiyDate();
        UserInformation userInformation = new UserInformation(
            user.getAccountHolder(),
            user.getEmail(),
            user.getMonthlyEarnings(),
            user.getPersonType().getPersonType(),
            overdraftLimit, 
            overdraftUsed,
            interestPayable,
            maturityDate,
            accountTypes,
            accountCards,
            balances,
            userTransactions
        );
        Map<String, String> dashboardData = new HashMap<>();
        dashboardData.put("holder", user.getAccountHolder());
        dashboardData.put("email", user.getEmail());
        dashboardData.put("monthly-earnings", String.valueOf(user.getMonthlyEarnings())); 
        dashboardData.put("person-type", user.getPersonType().getPersonType());
        dashboardData.put("overdraft-limit", overdraftLimit == null ? "" : String.valueOf(overdraftLimit));
        dashboardData.put("overdraft-used", overdraftUsed == null ? "" : String.valueOf(overdraftUsed));
        dashboardData.put("interest-payable", interestPayable == null ? "" : String.valueOf(interestPayable));
        dashboardData.put("maturity-date", maturityDate == null ? "" : maturityDate.toString());
        dashboardData.put("account-types", mapper.writeValueAsString(accountTypes));
        dashboardData.put("account-cards", mapper.writeValueAsString(accountCards));
        dashboardData.put("user-balances", mapper.writeValueAsString(balances));
        dashboardData.put("user-transactions", mapper.writeValueAsString(userTransactions));
        redis.opsForHash().putAll(key, dashboardData);
        redis.expire(key, Duration.ofHours(12));
        return userInformation;
    }

    public UserInformation getMyAccountInformation(Long id) {
        logger.info("Searching for the user account");
        Account account = getterService.getAccountFromDatabase(id);
        User user = account.getUser();
        securityService.evaluateIfIsTheFirst(null, user);
        logger.info("Preparing the data format");
        String key = "user-" + account.getId() + "information";
        UserInformation userInformation = null;
        try {
            String valueInRedis = (String) redis.opsForHash().get(key, "email");
            if (valueInRedis == null) {
                userInformation = isNotInRedis(account, user, key);
            } else {
                logger.info("Data that might be null");
                String overdraftLimitStr = (String) redis.opsForHash().get(key, "overdraft-limit");
                String overdraftUsedStr = (String) redis.opsForHash().get(key, "overdraft-used");
                String interesPayableStr = (String) redis.opsForHash().get(key, "interest-payable");
                String maturityDateStr = (String) redis.opsForHash().get(key, "maturity-date");
                logger.info("Preparing lists");
                String accountTypes = (String) redis.opsForHash().get(key, "account-types");
                String accountCards = (String) redis.opsForHash().get(key, "account-cards");
                String userTransactions = (String) redis.opsForHash().get(key, "user-transactions"); 
                String balancesStr = (String) redis.opsForHash().get(key, "user-balances");
                logger.info("Generathing the user information format");
                userInformation = new UserInformation(
                    (String) redis.opsForHash().get(key, "holder"),
                    valueInRedis,
                    new BigDecimal((String) redis.opsForHash().get(key, "monthly-earnings")),
                    (String) redis.opsForHash().get(key, "person-type"),
                    overdraftLimitStr.isEmpty() ? null : new BigDecimal(overdraftLimitStr),
                    overdraftUsedStr.isEmpty() ? null : new BigDecimal(overdraftUsedStr),
                    interesPayableStr.isEmpty() ? null : new BigDecimal(interesPayableStr),
                    maturityDateStr.isEmpty() ? null : LocalDateTime.parse(maturityDateStr),
                    mapper.readValue(accountTypes, new TypeReference<List<String>>() {}),
                    mapper.readValue(accountCards, new TypeReference<List<String>>() {}),
                    mapper.readValue(balancesStr, new TypeReference<List<BigDecimal>>() {}),
                    mapper.readValue(userTransactions, new TypeReference<List<UserTransactions>>() {})
                );
            }
        } catch (RedisConnectionFailureException | RedisSystemException e) {
            logger.warn("Redis connection not found: " + e.getMessage());
            userInformation = isNotInRedis(account, user, key);
        }
        return userInformation;
    }

    public List<UserTransactions> getMyTransactions(List<Account> accounts) {
        logger.info("Searching for the user transactions");
        List<UserTransactions> userTransactionsInformation = new ArrayList<>();
        for (Account account: accounts) {
            List<Transaction> transactions = transactionRepository.findAccounTransactions(account);
            for (Transaction transaction: transactions) {
                userTransactionsInformation.add(createUserTransaction(account, transaction));
            }
        }
        logger.info("The transactions format is ready");
        return userTransactionsInformation;
    }

    private UserTransactions createUserTransaction(Account account, Transaction transaction) {
        boolean isTransfer = transaction.getTransfer() != null;
        String payeeCard = null;
        String payerCard = null;
        if (isTransfer) {
            boolean isThePayer = account.getAccountCard()
                .equals(transaction.getAccount().getAccountCard());
            payeeCard = isThePayer ? 
                transaction.getTransfer().getPayee().getAccountCard() : 
                account.getAccountCard();
            payerCard = isThePayer ? 
                account.getAccountCard() : 
                transaction.getTransfer().getPayee().getAccountCard();
            payeeCard = "********" + payeeCard.substring(payeeCard.length() - 4);
            payerCard = "********" + payerCard.substring(payerCard.length() - 4);
        }
        return new UserTransactions(
            account.getAccountType().getAccountType(),
            transaction.getTransactionType().getTransactionType(),
            transaction.getAmount(),
            transaction.getTransactionDate(),
            isTransfer ? payeeCard : null,
            isTransfer ? payerCard : null
        );
    }

    public void setMyInformationInRedis(Object value, String operation, Account account, List<Account> accounts) {
        String key = "user-" + account.getId() + "information";
        switch (operation) {
            case "user-transactions":
                String userTransactionsString = (String) redis.opsForHash().get(key, "user-transactions");
                List<UserTransactions> userTransactions = mapper.readValue(
                    userTransactionsString, new TypeReference<List<UserTransactions>>() {});
                Transaction transaction = (Transaction) value;
                userTransactions.add(0, createUserTransaction(account, transaction));
                redis.opsForHash().put(key, "user-transactions", mapper.writeValueAsString(userTransactions));
                break;
            case "account-cards":
            case "account-types":
                String valueAsString = (String) redis.opsForHash().get(key, operation);
                List<String> valueAsListFromString = mapper.readValue(
                    valueAsString, new TypeReference<List<String>>() {});
                valueAsListFromString.add(((String) value));
                redis.opsForHash().put(key, operation, mapper.writeValueAsString(valueAsListFromString));
                break;
            case "user-balances":
                List<BigDecimal> balances = List.of();
                for (Account account2: accounts) balances.add(account2.getBalance());
                redis.opsForHash().put(key, operation, mapper.writeValueAsString(balances));
                break;
            default:
                redis.opsForHash().put(key, operation, String.valueOf(value));
                break;
        }
    }
}