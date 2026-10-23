package com.eripy.banking_system.dto.transactions;

import java.math.BigDecimal;

public record FlaggedDetail(
    BigDecimal max,
    BigDecimal monthly,
    BigDecimal period
) {}
