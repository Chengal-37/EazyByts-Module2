package com.stocktrading.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class PortfolioResponse {
    private List<Holding> holdings;
    // You might also want to include cash balance, total portfolio value, etc.

    // Constructor
    public PortfolioResponse(List<Holding> holdings) {
        this.holdings = holdings;
    }

    // Getters and Setters
    public List<Holding> getHoldings() {
        return holdings;
    }

    public void setHoldings(List<Holding> holdings) {
        this.holdings = holdings;
    }

    // Inner class for individual stock holdings
    public static class Holding {
        private String symbol;
        private String companyName; // Optional, but nice to display
        private Integer shares;
        private BigDecimal avgPrice;
        private BigDecimal currentPrice; // This would typically come from live market data
        private BigDecimal totalValue; // shares * currentPrice
        private BigDecimal gainLoss; // (currentPrice - avgPrice) * shares

        // Constructor
        public Holding(String symbol, String companyName, Integer shares, BigDecimal avgPrice, BigDecimal currentPrice, BigDecimal totalValue, BigDecimal gainLoss) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.shares = shares;
            this.avgPrice = avgPrice;
            this.currentPrice = currentPrice;
            this.totalValue = totalValue;
            this.gainLoss = gainLoss;
        }

        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public Integer getShares() { return shares; }
        public void setShares(Integer shares) { this.shares = shares; }
        public BigDecimal getAvgPrice() { return avgPrice; }
        public void setAvgPrice(BigDecimal avgPrice) { this.avgPrice = avgPrice; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getTotalValue() { return totalValue; }
        public void setTotalValue(BigDecimal totalValue) { this.totalValue = totalValue; }
        public BigDecimal getGainLoss() { return gainLoss; }
        public void setGainLoss(BigDecimal gainLoss) { this.gainLoss = gainLoss; }
    }
}