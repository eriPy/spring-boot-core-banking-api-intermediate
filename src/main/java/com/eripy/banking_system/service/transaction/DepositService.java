package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import java.math.RoundingMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.DepositRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.transactions.DepositTransaction;
import com.eripy.banking_system.entities.transactions.InterestChargeTransaction;
import com.eripy.banking_system.entities.transactions.PaymentMethod;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.ResourceNotFoundException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.transactions.DepositTransactionRepository;
import com.eripy.banking_system.repositories.transactions.InterestChargeTransactionRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.AccountService;
import com.eripy.banking_system.service.account.EmailService;
import com.eripy.banking_system.service.account.SecurityService;

@Service 
public class DepositService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final TransactionRepository transactionRepository;
    private final EmailService emailService;
    private final DepositTransactionRepository depositTransactionRepository;
    private final SecurityService securityService;
    private final AccountService accountService;
    private final CheckingAccountRepository checkingAccountRepository;
    private final InterestChargeTransactionRepository interestChargeTransactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(DepositService.class);
    
    public DepositService(
        GetterService getterService,
        AccountRepository accountRepository,
        TransactionRepository transactionRepository,
        EmailService emailService,
        DepositTransactionRepository depositTransactionRepository,
        SecurityService securityService,
        AccountService accountService,
        CheckingAccountRepository checkingAccountRepository,
        InterestChargeTransactionRepository interestChargeTransactionRepository
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.transactionRepository = transactionRepository;
        this.emailService = emailService;
        this.depositTransactionRepository = depositTransactionRepository;
        this.securityService = securityService;
        this.accountService = accountService;
        this.checkingAccountRepository = checkingAccountRepository;
        this.interestChargeTransactionRepository = interestChargeTransactionRepository;
    }

    @Transactional 
    public void processDeposit(Long id, DepositRequest depositRequest) {
        logger.info("Evaluating payment method");
        PaymentMethod pm = getterService.getPaymentMethod(depositRequest.paymentMethod());
        logger.info("Search user");
        Account account = getterService.getAccountFromDatabase(id);
        logger.info("User found");
        logger.info("Evaluating the account status");
        securityService.evaluateStatus(account);
        String act = account.getAccountType().getAccountType();
        Boolean depositEmail = true;
        if (act.equals("fixed_deposit_account")) {
            processToFixedDepositAccount(account);
        }
        if (act.equals("checking_account")) {
            depositRequest = processToCheckingAccount(account, depositRequest, depositEmail);
        }
        if (depositEmail) {
            logger.info("Process to the deposit");
            account.setBalance(account.getBalance().add(depositRequest.amount()));
            Transaction transaction = new Transaction(
                account,
                getterService.getTransactionType("deposit"),
                depositRequest.amount()
            );
            transactionRepository.save(transaction);
            depositTransactionRepository.save(new DepositTransaction(transaction, pm));
            emailService.sendEmail(
                account.getEmail(), 
                "Deposit successful", 
                "The deposit for $" + depositRequest.amount() + " has been made"
            );
            logger.info("Deposit successful");
        }
        accountRepository.save(account);
        accountService.setRedisUserInformation(account);
    }

    private void processToFixedDepositAccount(Account account) {
        logger.info("Evaluating conditions for a fixed deposit account");
        if (account.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            logger.warn(
                "A fixed deposit account cannot receive further deposits once a balance is introduced"
            );
            throw new InconsistenceDataException("Account problem");
        }
        logger.info("The account can still proceed with the deposit");
    }

    private DepositRequest processToCheckingAccount(Account account, DepositRequest request, Boolean depositEmail) {
        CheckingAccount ca = checkingAccountRepository.findByAccount(account)
            .orElseThrow(() -> new ResourceNotFoundException("Something went wrong"));
        boolean hasInterest = ca.getInterestPayable().compareTo(BigDecimal.ZERO) != 0;
        boolean hasOverdraftUsed = ca.getOverdraftUsed().compareTo(BigDecimal.ZERO) != 0;
        if (hasInterest || hasOverdraftUsed) {
            BigDecimal newOverdraft = null;
            BigDecimal newInterest = null;
            BigDecimal roundingInterest = ca.getInterestPayable().setScale(2, RoundingMode.HALF_UP);
            BigDecimal newAmount = request.amount();
            if (hasInterest) {
                boolean interestIsGreather = ca.getInterestPayable().compareTo(request.amount()) >= 0;
                newInterest = interestIsGreather ? ca.getInterestPayable().subtract(request.amount()) : BigDecimal.ZERO;
                newAmount = interestIsGreather ? BigDecimal.ZERO : request.amount().subtract(roundingInterest);
            }
            if (hasOverdraftUsed) {
                boolean overdraftIsGreather = ca.getOverdraftUsed().compareTo(newAmount) >= 0;
                newOverdraft = overdraftIsGreather ? ca.getOverdraftUsed().subtract(newAmount): BigDecimal.ZERO;
                newAmount = overdraftIsGreather ? BigDecimal.ZERO : newAmount.subtract(ca.getOverdraftUsed());
            }
            ca.setOverdraftUsed(newOverdraft);
            ca.setInterestPayable(newInterest);
            checkingAccountRepository.save(ca);
            BigDecimal interestCharged = request.amount().subtract(newAmount);
            Transaction t = new Transaction(
                account,
                getterService.getTransactionType("interest_charge"),
                interestCharged
            );
            transactionRepository.save(t);
            interestChargeTransactionRepository.save(new InterestChargeTransaction(
                t, 
                getterService.getPaymentMethod(request.paymentMethod())
            ));
            emailService.sendEmail(
                account.getEmail(), 
                "Interest charge", 
                "The interest charge for $" + interestCharged + " has been made"
            );
            if (newAmount.compareTo(BigDecimal.ZERO) == 0) depositEmail = false;
            return new DepositRequest(newAmount, request.token(), request.paymentMethod());
        }
        return request;
    }
}
