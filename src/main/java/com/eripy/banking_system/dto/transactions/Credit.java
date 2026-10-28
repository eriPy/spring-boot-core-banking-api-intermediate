package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;
import com.eripy.banking_system.entities.accounts.Account;

public interface Credit {
    Account getAccount();
    BigDecimal getCreditInterestRate();
}
