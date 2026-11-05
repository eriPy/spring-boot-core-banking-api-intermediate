package com.eripy.banking_system.service.account;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import com.eripy.banking_system.dto.account.CreateAccoutRequest;
import com.eripy.banking_system.dto.account.SignUpRequest;
import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.AccountsType;
import com.eripy.banking_system.entities.accounts.CheckingAccount;
import com.eripy.banking_system.entities.accounts.FixedDepositAccount;
import com.eripy.banking_system.entities.accounts.PayrollAccount;
import com.eripy.banking_system.entities.accounts.PersonType;
import com.eripy.banking_system.entities.accounts.SavingsAccount;
import com.eripy.banking_system.entities.accounts.User;
import com.eripy.banking_system.exceptions.FormatDataException;
import com.eripy.banking_system.repositories.accounts.AccountRepository;
import com.eripy.banking_system.repositories.accounts.CheckingAccountRepository;
import com.eripy.banking_system.repositories.accounts.FixedDepositAccountRepository;
import com.eripy.banking_system.repositories.accounts.PayrollAccountRepository;
import com.eripy.banking_system.repositories.accounts.SavingsAccountRepository;
import com.eripy.banking_system.repositories.accounts.UserRepository;

@Service 
public class RecordService {
    private final UserRepository userRepository;
    private final AccountRepository accountRepository;
    private final SavingsAccountRepository savingsAccountRepository;
    private final CheckingAccountRepository checkingAccountRepository;
    private final FixedDepositAccountRepository fixedDepositAccountRepository;
    private final PayrollAccountRepository payrollAccountRepository;
    private static final Logger logger = LoggerFactory.getLogger(RecordService.class);

    public RecordService(
        UserRepository userRepository,
        AccountRepository accountRepository,
        SavingsAccountRepository savingsAccountRepository,
        CheckingAccountRepository checkingAccountRepository,
        FixedDepositAccountRepository fixedDepositAccountRepository,
        PayrollAccountRepository payrollAccountRepository
    ) {
        this.userRepository = userRepository;
        this.accountRepository = accountRepository;
        this.savingsAccountRepository = savingsAccountRepository;
        this.checkingAccountRepository = checkingAccountRepository;
        this.fixedDepositAccountRepository = fixedDepositAccountRepository;
        this.payrollAccountRepository = payrollAccountRepository;
    }

    public Long registerUser(
        CreateAccoutRequest request, 
        AccountsType accountType, 
        SignUpRequest userData,
        String password,
        PersonType personType,
        String accountCard
    ) {
        logger.info("Creating the user");
        User user = new User(
            request.accountHolder(),
            userData.idCard(),
            userData.email(),
            password,
            personType,
            request.monthlyEarnings()
        );
        userRepository.save(user);
        logger.info("Creating the account");
        Account account = new Account(user, accountType, accountCard);
        accountRepository.save(account);
        logger.info("Saving the account type");
        switch (userData.accountType()) {
            case "savings_account":
                savingsAccountRepository.save(new SavingsAccount(account));
                break;
            case "checking_account":
                checkingAccountRepository.save(new CheckingAccount(account, personType.getOverdraft().getOverdraftValue()));
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
        return account.getId();
    }

    public Account registerAccount() {
        logger.info("Evaluating if the account exists");
        return null;
    }
}
