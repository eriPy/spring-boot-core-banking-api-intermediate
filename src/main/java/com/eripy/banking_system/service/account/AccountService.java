package com.eripy.banking_system.service.account;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.account.AccountInformationResponse;
import com.eripy.banking_system.dto.manageAccount.AccountHolder;
import com.eripy.banking_system.dto.manageAccount.MonthlyEarnings;
import com.eripy.banking_system.dto.manageAccount.Password;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.dto.manageAccount.SetEmail;
import com.eripy.banking_system.dto.manageAccount.SetPersonType;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.User;
import com.eripy.banking_system.entities.security.PenalizedAccount;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.accounts.UserRepository;
import com.eripy.banking_system.repositories.penalties.BlackListRepository;
import com.eripy.banking_system.repositories.penalties.PenalizedAccountRepository;
import com.eripy.banking_system.service.GetterService;

@Service 
public class AccountService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final EvaluateFormatService evaluateFormatService;
    private final PenalizedAccountRepository penalizedAccountRepository;
    private final BlackListRepository blackListRepository;
    private final EmailService emailService;
    private final UserRepository userRepository;
    private final CheckingAccountRepository checkingAccountRepository;
    private static final Logger logger = LoggerFactory.getLogger(AccountService.class);

    public AccountService(
        GetterService getterService,
        AccountRepository accountRepository,
        EvaluateFormatService evaluateFormatService,
        PenalizedAccountRepository penalizedAccountRepository,
        BlackListRepository blackListRepository,
        EmailService emailService,
        UserRepository userRepository,
        CheckingAccountRepository checkingAccountRepository
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.evaluateFormatService = evaluateFormatService;
        this.penalizedAccountRepository = penalizedAccountRepository;
        this.blackListRepository = blackListRepository;
        this.emailService = emailService;
        this.userRepository = userRepository;
        this.checkingAccountRepository = checkingAccountRepository;
    }

    public AccountInformationResponse getUserInformation(Long id) {
        logger.info("Search the user information");
        if (!accountRepository.existsById(id)) {
            logger.warn("The user not exists");
            throw new ResourceNotFoundException("Invalid token");
        }
        logger.info("Search the user");
        User information = getterService.getUserFromDataBase(id);
        List<Account> accounts = getterService.getAccounts(information);
        return evaluateFormatService.getInformationFormat(information, accounts);
    }

    public void unfreeze(Account account) {
        logger.info("Search if user is freezed");
        if (account.getAccountStatus() == AccountStatus.frozen) {
            PenalizedAccount pa = penalizedAccountRepository
                .findFreezed(account, LocalDateTime.now())
                .orElse(null);
            if (pa == null) {
                logger.info("The user's account can now be unfrozen");
                account.setAccountStatus(AccountStatus.active);
                accountRepository.save(account);
                return;
            }
            logger.info("The user's account must remain frozen");
        }
    }

    public void inBlackList(User user) {
        logger.info("Search if user in black list");
        if (blackListRepository.existsByUser(user)) {
            logger.info("This user is on the blacklist");
            throw new InconsistenceDataException("Invalid access");
        }
    } 

    private <T> void set(
        Long id,
        T newValue,
        Function<User, T> getter,
        BiConsumer<User, T> setter,
        String topic,
        Account references
    ) {
        logger.info("Searching for the account in database");
        Account account = references != null ? references : getterService.getAccountFromDatabase(id);
        logger.info("Comparing the " + topic +  " with the registered " + topic);
        User user = account.getUser();
        T oldValue = getter.apply(user);
        if (java.util.Objects.equals(oldValue, newValue)) {
            logger.warn("The new " + topic + " cannot be equal to the registered " + topic);
            throw new InconsistenceDataException("Invalid " + topic);
        }
        setter.accept(user, newValue);
        userRepository.save(user);
        logger.info("The " + topic + " was updated in the database");
        emailService.sendEmail(
            user.getEmail(), 
            topic + "was updated", 
            "Your " + topic + " was updated sucessfuly from: " + oldValue + " to: " + newValue
        );
    }

    @Transactional 
    public void setEmail(SetEmail request, Long id) {
        set(id, request.email(), User::getEmail, User::setEmail, "Email address", null);
    }

    @Transactional 
    public void setPersonType(SetPersonType request, Long id) {
        logger.info("Searching for the person type in database");
        PersonType pt = getterService.getPersonTypeFromCache(request.personType());
        logger.info("Checking whether the account is a checking account");
        Account account = getterService.getAccountFromDatabase(id);
        if (account.getCheckingAccount() != null) {
            logger.info("Setting the current overdraft limit");
            CheckingAccount ca = account.getCheckingAccount();
            BigDecimal newLimit =  pt.getOverdraft().getOverdraftValue().subtract(ca.getOverdraftUsed());
            ca.setOverdraftLimit(
               newLimit.compareTo(BigDecimal.ZERO) > 0 ?
            newLimit : BigDecimal.ZERO
            );
            checkingAccountRepository.save(ca);
        }
        set(id, pt, User::getPersonType, User::setPersonType, "Person type", account);
    }

    @Transactional 
    public void setPassword(Password password, Long id) {
        set(id, password.password(), User::getPassword, User::setPassword, "Password", null);
    }

    @Transactional 
    public void setMonthlyEarnings(MonthlyEarnings request, Long id) {
        set(
            id, 
            request.monthlyEarnings(), 
            User::getMonthlyEarnings, 
            User::setMonthlyEarnings, 
            "Monthly earnings", 
            null
        );
    }

    @Transactional 
    public void setAccountHolder(AccountHolder request, Long id) {
        set(
            id, 
            request.accountHolder(), 
            User::getAccountHolder, 
            User::setAccountHolder, 
            "Account holder", 
            null
        );
    }

    @Transactional
    public void closeAccount(Long id) {
        logger.info("Searching for the account");
        Account account = getterService.getAccountFromDatabase(id);
        logger.info("Checking whether the account has a balance to close the account");
        if (account.getBalance().compareTo(BigDecimal.ZERO) != 0) {
            logger.warn("The account still has a balance. You cannot close an account with an outstanding balance");
            throw new InconsistenceDataException("Invalid request");
        }
        logger.info("Checking whether the account is a checking account");
        if (account.getCheckingAccount() != null) {
            logger.info("Checking whether the account has any outstanding interest payments");
            CheckingAccount ca = account.getCheckingAccount();
            boolean hasInterest = ca.getInterestPayable().compareTo(BigDecimal.ZERO) != 0;
            boolean hasOverdraftUsed = ca.getOverdraftInterestRate().compareTo(BigDecimal.ZERO) != 0;
            if (hasInterest || hasOverdraftUsed) {
                logger.warn("The account still has outstanding interest payments");
                throw new InconsistenceDataException("Invalid request");
            }
        }
        logger.info("Closing the account");
        account.setAccountStatus(AccountStatus.closed);
        accountRepository.save(account);
        logger.info("The account was closed");
        emailService.sendEmail(
            account.getUser().getEmail(), 
            "Account " + account.getAccountType() + " closure", 
            "Your account was successfully closed. You can no longer use your account"
        );
    }
}