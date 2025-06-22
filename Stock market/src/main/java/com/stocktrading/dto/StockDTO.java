package com.stocktrading.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class StockDTO {
    private Long id;
    private String symbol;
    private String companyName;
    private BigDecimal currentPrice;
    private BigDecimal previousClose;
    private BigDecimal dayHigh;
    private BigDecimal dayLow;
    private Long volume;

    // --- RENAMED 'change' to 'priceChange' ---
    private BigDecimal priceChange;
    // ------------------------------------------

    private BigDecimal changePercent;
    private LocalDateTime lastUpdated;
}