package com.eripy.banking_system.entities.transactions;

import com.eripy.banking_system.entities.accounts.Account;
import com.eripy.banking_system.entities.accounts.User;

public record AccountsToTransfer(
    Account accountToProcessTransfer,
    User user,
    Account accountToTransfer
) {}