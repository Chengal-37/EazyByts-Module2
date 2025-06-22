package com.stocktrading.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class MarketResponse {
    private List<Stock> stocks;

    // Constructor
    public MarketResponse(List<Stock> stocks) {
        this.stocks = stocks;
    }

    // Getters and Setters
    public List<Stock> getStocks() {
        return stocks;
    }

    public void setStocks(List<Stock> stocks) {
        this.stocks = stocks;
    }

    // Inner class for individual stock data
    public static class Stock {
        private String symbol;
        private String companyName;
        private BigDecimal price;
        private BigDecimal change; // Percentage change
        private Long volume;

        // Constructor
        public Stock(String symbol, String companyName, BigDecimal price, BigDecimal change, Long volume) {
            this.symbol = symbol;
            this.companyName = companyName;
            this.price = price;
            this.change = change;
            this.volume = volume;
        }

        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public BigDecimal getPrice() { return price; }
        public void setPrice(BigDecimal price) { this.price = price; }
        public BigDecimal getChange() { return change; }
        public void setChange(BigDecimal change) { this.change = change; }
        public Long getVolume() { return volume; }
        public void setVolume(Long volume) { this.volume = volume; }
    }
}
