package com.stocktrading.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data // Lombok will generate getters and setters for all fields
public class PortfolioItemDTO {
    private Long holdingId; // ID of the PortfolioHolding entity
    private Long stockId;
    private String symbol;
    private String companyName;

    // --- RENAMED FIELD ---
    private Integer shares; // Renamed from 'quantity' to 'shares' to match PortfolioHolding entity

    private BigDecimal averageBuyPrice;
    private BigDecimal currentPrice; // Fetched dynamically
    private BigDecimal totalValue; // Calculated (shares * currentPrice)
    private BigDecimal gainLoss; // Calculated
    private BigDecimal gainLossPercent; // Calculated
}