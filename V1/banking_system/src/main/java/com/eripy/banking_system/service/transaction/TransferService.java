package com.eripy.banking_system.service.transaction;

import java.math.BigDecimal;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.eripy.banking_system.dto.transactions.TransferRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountStatus;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.entities.accounts.User;
import com.eripy.banking_system.entities.transactions.AccountsToTransfer;
import com.eripy.banking_system.exceptions.InconsistenceDataException;
import com.eripy.banking_system.exceptions.InvalidQuantity;
import com.eripy.banking_system.exceptions.ProcessException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.service.GetterService;
import com.eripy.banking_system.service.account.DashboardService;
import com.eripy.banking_system.service.account.EmailService;

@Service 
public class TransferService {
    private final GetterService getterService;
    private final AccountRepository accountRepository;
    private final EmailService emailService;
    private final DashboardService dashboardService;
    private static final Logger logger = LoggerFactory.getLogger(TransferService.class);

    public TransferService(
        GetterService getterService,
        AccountRepository accountRepository,
        EmailService emailService,
        DashboardService dashboardService
    ) {
        this.getterService = getterService;
        this.accountRepository = accountRepository;
        this.emailService = emailService;
        this.dashboardService = dashboardService;
    }

    @Transactional 
    public void processTransfer(Long id, TransferRequest request, List<Account> accountsReference) {
        logger.info("Processing the transfer");
        AccountsToTransfer accounts = getAccountsToProcessTransfer(
            id, request.accountCardToTransfer(), request.accountType());
        Account account = accounts.accountToProcessTransfer();
        Account accountToTransfer = accounts.accountToTransfer();
        User user = accounts.user();
        logger.info("Evaluting if the account that receives the transfer is valid");
        if (!evaluateAccountToTransfer(accountToTransfer)) throw new ProcessException("Invalid data");
        logger.info("Evaluating the condition of the account that process the transfer");
        if (!evaluateAccountToProcessTransfer(accountToTransfer, request.amount())) {
            throw new InconsistenceDataException("Invalid data request");
        }
        withdrawalToPayer(account, user, request.amount());
        depositToPayee(accountToTransfer, request.amount());
        logger.info("Evaluating if the amount is enogth");
    }

    public AccountsToTransfer getAccountsToProcessTransfer(Long id, String accountCard, String accountType) {
        Account references = getterService.getAccountFromDatabase(id);
        User user = references.getUser();
        boolean itsTheSame = references.getAccountType().getAccountType().equals(accountType);
        Account account = itsTheSame ? references :
            getterService.getAccountFromDatabaseForAccountType(user, accountType);
        logger.info("Verifing the account card to process the transfer and the account that receives the transfer");
        Account accountToTransfer = accountRepository.findByAccountCard(accountCard)
            .orElseThrow(() -> {
                logger.warn("The account card received is invalid");
                throw new InconsistenceDataException("Invalid data request");
            });
        return new AccountsToTransfer(account, user, accountToTransfer);
    }

    public boolean evaluateAccountToTransfer(Account account) {
        if (account.getFixedDepositAccount() != null) {
            logger.warn("A fixed deposit account cannot process transfers");
            return false;
        }
        if (account.getAccountStatus() != AccountStatus.active) {
            logger.warn("This account cannot process transactions because isn't active");
            return false;
        }
        return true;
    }

    public boolean evaluateAccountToProcessTransfer(Account account, BigDecimal amount) {
        if (account.getSavingsAccount() != null) {
            logger.info("Evaluating conditions for a savings account");
            SavingsAccount sa = account.getSavingsAccount();
            if (sa.getTransactionsWithoutFee() == 0) {
                logger.warn("Insuficient transactions without fee");
                throw new InconsistenceDataException(
                    "A transfer cannot process if the user has no suficient transactions without pay fee");
            }
        }
        if (account.getCheckingAccount() != null) {
            logger.info("Evaluating conditions for a checking account");
            CheckingAccount ca = account.getCheckingAccount();
            boolean hasInterestToPay = ca.getInterestPayable().compareTo(BigDecimal.ZERO) != 0 ||
                ca.getOverdraftUsed().compareTo(BigDecimal.ZERO) != 0;
            if (hasInterestToPay) {
                logger.warn("This account has interest to pay");
                throw new ProcessException("This account cannot process transfers yet");
            }   
        }
        if (account.getBalance().compareTo(amount) < 0) {
            logger.warn("The balance is insuficient");
            throw new InvalidQuantity("Invalid data request");
        }
        return true;
    }

    public void withdrawalToPayer(Account account, User user, BigDecimal amount) {
        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        dashboardService.setMyInformationInRedis(account.getBalance(), "null", account);
        emailService.sendEmail(
            user.getEmail(), 
            "", 
            """
            """
        );
    }

    public void depositToPayee(Account account, BigDecimal amount) {
        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        dashboardService.setMyInformationInRedis(account.getBalance(), null, account);
        emailService.sendEmail(
            account.getUser().getEmail(), 
            "", 
            """
            """
        );
    }
}
