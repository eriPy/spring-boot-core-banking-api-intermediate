package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.WithdrawalRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.entities.security.Overdraft;
import com.eripy.banking_system.entities.transactions.FeeTransaction;
import com.eripy.banking_system.entities.transactions.InterestAccrualTransaction;
import com.eripy.banking_system.entities.transactions.PaymentMethod;
import com.eripy.banking_system.entities.transactions.Transaction;
import com.eripy.banking_system.entities.transactions.TransactionType;
import com.eripy.banking_system.entities.transactions.WithdrawalTransaction;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.InvalidOperation;
import com.eripy.banking_system.exceptions.InvalidQuantity;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
import com.eripy.banking_system.repositories.transactions.FeeTransactionRepository;
import com.eripy.banking_system.repositories.transactions.InterestAccrualTransactionRepository;
import com.eripy.banking_system.repositories.transactions.TransactionRepository;
import com.eripy.banking_system.repositories.transactions.WithdrawalTransactionRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.EmailService;
import com.eripy.banking_system.service.account.SecurityService;

@Service 
public class WithdrawalService {
    private final GetterService getterService;
    private final EmailService emailService;
    private final TransactionRepository transactionRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final AccountRepository accountRepository;
    private final CheckingAccountRepository checkingAccountRepository;
    private final WithdrawalTransactionRepository withdrawalTransactionRepository;
    private final SecurityService securityService;
    private final FeeTransactionRepository feeTransactionRepository;
    private final InterestAccrualTransactionRepository interestAccrualTransactionRepository;
    private static final Logger logger = LoggerFactory.getLogger(WithdrawalService.class);

    public WithdrawalService(
        GetterService getterService,
        EmailService emailService,
        TransactionRepository transactionRepository,
        SavingsAccountRepository savingsAccountRepository,
        AccountRepository accountRepository,
        CheckingAccountRepository checkingAccountRepository,
        WithdrawalTransactionRepository withdrawalTransactionRepository,
        SecurityService securityService,
        FeeTransactionRepository feeTransactionRepository,
        InterestAccrualTransactionRepository interestAccrualTransactionRepository
    ) {
        this.getterService = getterService;
        this.emailService = emailService;
        this.transactionRepository = transactionRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.accountRepository = accountRepository;
        this.checkingAccountRepository = checkingAccountRepository;
        this.withdrawalTransactionRepository = withdrawalTransactionRepository;
        this.securityService = securityService;
        this.feeTransactionRepository = feeTransactionRepository;
        this.interestAccrualTransactionRepository = interestAccrualTransactionRepository;
    }

    @Transactional 
    public void processWithdrawal(Long id, WithdrawalRequest request) {
        logger.info("Evaluating the payment method");
        PaymentMethod pm = getterService.getPaymentMethod(request.paymentMethod());
        logger.info("Search the user");
        Account account = getterService.getAccountFromDatabase(id);
        securityService.evaluateStatus(account);
        String act = account.getAccountType().getAccountType();
        TransactionType tt = getterService.getTransactionType("withdrawal");
        switch (act) {
            case "fixed_deposit_account":
                logger.info("Process a withdrawal for a fixed deposit account");
                processToFixedDepositAccount(account, request.amount());
                break;
            case "savings_account":
                logger.info("Process a withdrawal for a saving account");
                processToSavingAccount(account, request);
                break;
            case "checking_account":
                logger.info("Process a withdrawal for a cheking account");
                processToChekingAccount(account, request.amount());
                break;
        }
        
        Transaction transaction = new Transaction(account, tt, request.amount());
        transactionRepository.save(transaction);
        withdrawalTransactionRepository.save(new WithdrawalTransaction(transaction, pm));
        accountRepository.save(account);
        emailService.sendEmail(
            account.getUser().getEmail(),
            "Withdrawal successful",
            "The withdrawal for $" + request.amount() + " has been made"
        );
    }

    public void processToFixedDepositAccount(Account account, BigDecimal amount) {
        if (account.getBalance().compareTo(BigDecimal.ZERO) == 0) {
            logger.info("This account has already withdrawn all of its balance");
            throw new InconsistenceDataException("Invalid data");
        }
        FixedDepositAccount fda = account.getFixedDepositAccount();
        boolean isNotTime = fda.getMaturiyDate().isAfter(LocalDateTime.now());
        if (isNotTime) {
            logger.warn("Cannot withdrawal yet");
            throw new InvalidOperation("Invalid operation");
        }
        boolean notAreEquals = account.getBalance().compareTo(amount) != 0;
        if (notAreEquals) {
            logger.warn("The amount must be equals to the balance");
            throw new InvalidQuantity("Invalid data");
        }
        logger.warn("interest receivable charged");
        account.setBalance(BigDecimal.ZERO);
        logger.info("Balance update");
        emailService.sendEmail(
            account.getUser().getEmail(),
            "Your fixed deposit account has been redeemed",
            "You have successfully collected the balance and interest earned from your fixed deposit account"
        );
    }

    public void processToSavingAccount(Account account, WithdrawalRequest request) {
        logger.info("Evaluating the balance");
        if (account.getBalance().compareTo(request.amount()) < 0) {
            logger.info("Insufficient balance to process the withdrawal");
            throw new InvalidQuantity("Invalid data");
        }
        logger.info("Search the saving account of the user");
        SavingsAccount sa = getterService.getSavingsAccountFromDatabase(account.getId());
        if (sa.getTransactionsWithoutFee() == 0) { 
            logger.info("Validate the balance available");
            if (account.getBalance().compareTo(request.amount().add(sa.getExcessTransactionFee())) < 0) {
                logger.warn("Insufficient balance to pay the fee");
                throw new InvalidQuantity("Invalid data");
            }
            logger.info("The user must pay fee");
            account.setBalance(account.getBalance().subtract(sa.getExcessTransactionFee()));
            logger.info("The fee was charged");
            Transaction t = new Transaction(
                account, 
                getterService.getTransactionType("fee"), 
                sa.getExcessTransactionFee()
            );
            transactionRepository.save(t);
            feeTransactionRepository.save(new FeeTransaction(
                t, 
                getterService.getPaymentMethod(request.paymentMethod())
            ));
            logger.info("Fee transaction saved");
            emailService.sendEmail(
                account.getUser().getEmail(),
                "Fee charged",
                "You has been pay a fee for $" + sa.getExcessTransactionFee()
            );
        } else {
            int val = sa.getTransactionsWithoutFee() - 1;
            sa.setTransactionsWithoutFee(val);
            savingsAccountRepository.save(sa);
            logger.info("Fee-free transaction recorded");
            emailService.sendEmail(
                account.getUser().getEmail(),
                "Saving account transaction free-fee",
                "You have " + sa.getTransactionsWithoutFee() + " transactions free-fee"
            );
        }
        account.setBalance(account.getBalance().subtract(request.amount()));
    }

    public void processToChekingAccount(Account account, BigDecimal amount) {
        logger.info("Search the cheking account of the user");
        CheckingAccount ca = account.getCheckingAccount();
        logger.info("Evaluating the interest to charge");
        Overdraft overdraft = getterService.getOverdraft(account.getUser().getPersonType());
        if (overdraft.getOverdraftValue().multiply(BigDecimal.valueOf(2)).compareTo(ca.getInterestPayable()) < 0) {
            logger.info("The account pays a lot of interest");
            throw new InvalidOperation("Invalid operation");
        }
        logger.warn("Evaluate the available quantity");
        if (account.getBalance().compareTo(amount) < 0) {
            logger.info("The balace is insufficient");
            BigDecimal previusOverdraftUsed = ca.getOverdraftUsed();
            logger.info("Evaluate if the balance with the overdraft is sufficient");
            BigDecimal reference = account.getBalance().add(ca.getOverdraftLimit());
            if (reference.compareTo(amount) < 0) {
                logger.warn("Insufficiet overdraft to process the withdrawal");
                throw new InvalidQuantity("Invalid data");
            }
            logger.info("Getting the new Overdraft used");
            BigDecimal newLimit = reference.subtract(amount).subtract(account.getBalance());
            logger.info("Getting the overdraft to use");
            BigDecimal used = ca.getOverdraftLimit().subtract(newLimit);
            logger.info("Processing the new data account");
            ca.setOverdraftLimit(newLimit);
            ca.setOverdraftUsed(ca.getOverdraftUsed().add(used));
            account.setBalance(BigDecimal.ZERO);
            checkingAccountRepository.save(ca);
            Transaction t = new Transaction(
                account, 
                getterService.getTransactionType("interest_accrual"),
                used
            );
            transactionRepository.save(t);
            interestAccrualTransactionRepository.save(new InterestAccrualTransaction(
                t, 
                getterService.getPaymentMethod("system"), 
                previusOverdraftUsed, 
                used.add(previusOverdraftUsed), 
                ca.getInterestPayable(), 
                ca.getInterestPayable()
            ));
            emailService.sendEmail(
                account.getUser().getEmail(), 
                "overdraft usage", 
                "You have used $" + used + 
                " as an overdraft; your current overdraft is $" + used.add(previusOverdraftUsed)
            );
        } else {
            logger.info("Sufficient balance to withdrawal");
            account.setBalance(account.getBalance().subtract(amount));
        }
    }
}
