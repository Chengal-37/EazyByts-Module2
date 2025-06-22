package com.stocktrading.payload.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

// Make sure you have lombok dependencies if you are using @Data, @AllArgsConstructor, @NoArgsConstructor
// If not, you will need to manually write constructors, getters, and setters.

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryResponse {
    private Long transactionId;
    private String stockSymbol;
    private String stockName;
    private String transactionType; // e.g., "BUY", "SELL"
    private Integer quantity;
    private BigDecimal priceAtTrade;
    private LocalDateTime timestamp;
    private BigDecimal totalAmount; // quantity * priceAtTrade
}
