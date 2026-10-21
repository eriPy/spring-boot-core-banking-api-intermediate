package com.eripy.banking_system.event;

import com.eripy.banking_system.entities.accounts.Account;

public record UserBehaviorAnalyzedEvent(Account account) {}
