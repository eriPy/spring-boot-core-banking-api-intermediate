package com.eripy.banking_system.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.eripy.banking_system.dto.dashboard.UserInformation;
import com.eripy.banking_system.service.account.DashboardService;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;

@RestController 
@RequestMapping("/api/dashboard")
@SecurityRequirement(name = "bearerAuth") 
public class DashboardController {
    private final DashboardService dashboardService;

    public DashboardController(
        DashboardService dashboardService
    ) {
        this.dashboardService = dashboardService;
    }

    public UserInformation getMyInformation(
        Authentication authentication
    ) {
        Long id = Long.parseLong(authentication.getName());
        UserInformation userInformation = dashboardService.getMyAccountInformation(id);
        return userInformation;
    }
}
