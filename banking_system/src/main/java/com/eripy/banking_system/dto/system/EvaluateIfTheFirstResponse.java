package com.eripy.banking_system.dto.system;

import java.util.List;
import com.eripy.banking_system.entities.accounts.Account;

public record EvaluateIfTheFirstResponse(
    boolean res,
    List<Account> accounts
) {}
