package com.stocktrading.controller;

import com.stocktrading.model.PortfolioHolding;
import com.stocktrading.model.Stock;
import com.stocktrading.model.User;
import com.stocktrading.payload.response.PortfolioResponse;
import com.stocktrading.payload.response.PortfolioResponse.Holding;
import com.stocktrading.repository.PortfolioHoldingRepository; // Import your repo // Import StockRepository to get current price
import com.stocktrading.repository.UserRepository; // Import UserRepository to fetch User object
import com.stocktrading.service.impl.UserDetailsImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired; // For @Autowired
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.math.RoundingMode; // For BigDecimal rounding
import java.util.ArrayList; // For building the list of holdings
import java.util.List; // For Optional results

@RestController
@RequestMapping("/api")
public class PortfolioController {

    private static final Logger logger = LoggerFactory.getLogger(PortfolioController.class);

    // --- AUTOWIRE THE REPOSITORIES YOU NEED ---
    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;
    @Autowired
    private UserRepository userRepository; // To get the User object for findByUser // To get current stock prices

    @GetMapping("/portfolio")
    @PreAuthorize("hasRole('USER') or hasRole('ADMIN')")
    public ResponseEntity<PortfolioResponse> getPortfolioData() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Long userId = userDetails.getId();

        logger.info("Fetching actual portfolio data for user ID: {}", userId);

        // 1. Fetch the User entity
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // 2. Fetch all portfolio holdings for this user
        List<PortfolioHolding> rawHoldings = portfolioHoldingRepository.findByUser(user);

        List<Holding> portfolioHoldings = new ArrayList<>();

        // 3. Convert PortfolioHolding entities to PortfolioResponse.Holding DTOs
        for (PortfolioHolding ph : rawHoldings) {
            Stock stock = ph.getStock(); // Assuming lazy loading works, or ph.getStock() will trigger fetch
                                        // If Stock object inside PortfolioHolding is not fully loaded,
                                        // you might need to fetch it explicitly:
                                        // stockRepository.findById(ph.getStock().getId()).orElse(null);

            BigDecimal currentPrice = stock.getCurrentPrice();
            if (currentPrice == null) {
                // Log a warning if current price is null for some reason, and skip this holding or use a default
                logger.warn("Stock {} has null current price. Skipping in portfolio display.", stock.getSymbol());
                continue; // Skip this holding if no valid current price
            }

            BigDecimal shares = BigDecimal.valueOf(ph.getShares());
            BigDecimal avgPrice = ph.getAverageBuyPrice();

            BigDecimal totalCurrentValue = shares.multiply(currentPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal totalInvestedValue = shares.multiply(avgPrice).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gainLoss = totalCurrentValue.subtract(totalInvestedValue).setScale(2, RoundingMode.HALF_UP);

            // Create the Holding DTO
            Holding holding = new Holding(
                stock.getSymbol(),
                stock.getCompanyName(),
                ph.getShares(),
                avgPrice,
                currentPrice,
                totalCurrentValue,
                gainLoss
            );
            portfolioHoldings.add(holding);
        }

        PortfolioResponse response = new PortfolioResponse(portfolioHoldings);
        return ResponseEntity.ok(response);
    }
}