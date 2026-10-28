package com.eripy.banking_system.dto.account;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public record AccountInformationResponse(
    String holder,
    String email,
    String idCard,
    String personType,
    BigDecimal monthlyEarnings,
    List<Map<Long, String>> accounts
) {}
