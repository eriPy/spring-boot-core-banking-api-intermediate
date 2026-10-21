package com.eripy.banking_system.dto.account;

import java.util.List;

public record AccountInformationResponse(
    String holder,
    String email,
    String idCard,
    String personType,
    List<AccountGeneral> general
) {}
