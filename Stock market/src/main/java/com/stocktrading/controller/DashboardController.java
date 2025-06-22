package com.stocktrading.controller;

import com.stocktrading.payload.response.DashboardResponse;
import com.stocktrading.payload.response.DashboardResponse.PerformanceData;
import com.stocktrading.service.impl.UserDetailsImpl;

import org.slf4j.Logger; // Import Logger
import org.slf4j.LoggerFactory; // Import LoggerFactory

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "http://localhost:8080", maxAge = 3600)
public class DashboardController {

    private static final Logger logger = LoggerFactory.getLogger(DashboardController.class); // Initialize logger

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<DashboardResponse> getDashboardData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        // Use the userId here to remove the warning.
        // In a real application, you would pass this userId to a service method
        // to fetch data specific to this user.
        logger.info("Fetching dashboard data for user ID: {}", userId);

        // --- Mock Data for Demonstration ---
        BigDecimal mockTotalValue = new BigDecimal("15245.78");

        List<String> mockLabels = Arrays.asList("Jan", "Feb", "Mar", "Apr", "May", "Jun");
        List<BigDecimal> mockValues = Arrays.asList(
            new BigDecimal("10000"),
            new BigDecimal("11500"),
            new BigDecimal("12300"),
            new BigDecimal("13800"),
            new BigDecimal("14500"),
            new BigDecimal("15245.78")
        );
        PerformanceData mockPerformance = new PerformanceData(mockLabels, mockValues);

        DashboardResponse response = new DashboardResponse(mockTotalValue, mockPerformance);

        return ResponseEntity.ok(response);
    }
}