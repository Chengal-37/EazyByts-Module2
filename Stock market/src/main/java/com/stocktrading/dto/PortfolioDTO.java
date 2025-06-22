package com.stocktrading.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data // Lombok will generate getters and setters for all fields
public class PortfolioDTO {
    private Long id; // This might be null or refer to the first holding's ID if useful
    private Long userId;
    private List<PortfolioItemDTO> holdings;
    private BigDecimal totalValue; // Calculated
    private BigDecimal totalInvestment; // Calculated
    private BigDecimal totalGainLoss; // Calculated
    private BigDecimal totalGainLossPercent; // Calculated

    // --- NEW FIELD ---
    private BigDecimal cashBalance; // Added to show user's available cash
}