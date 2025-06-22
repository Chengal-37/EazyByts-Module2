package com.stocktrading.payload.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TradeRequest {
    @NotBlank(message = "Stock symbol cannot be empty")
    private String stockSymbol; // Renamed to match frontend's 'stockSymbol'

    // Removed 'type' field because the endpoint /trades/buy implies type 'BUY'

    @NotNull(message = "Quantity cannot be null")
    @Min(value = 1, message = "Quantity must be at least 1")
    private Integer quantity;

    // Getters and Setters
    public String getStockSymbol() { // Renamed getter
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) { // Renamed setter
        this.stockSymbol = stockSymbol;
    }

    // Removed getType() and setType() methods

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }
}