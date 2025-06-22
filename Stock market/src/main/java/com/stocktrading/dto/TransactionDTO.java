package com.stocktrading.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
public class TransactionDTO {
    private Long id;
    private Long userId;
    private Long stockId;

    // --- Added for display/information in the DTO ---
    private String symbol;
    private String companyName;

    // --- Nested Enum for Transaction Type ---
    // This enum is specific to the DTO and is nested within the DTO class.
    public enum TransactionType {
        BUY,
        SELL
    }
    private TransactionType type; // Using the nested enum

    private Integer quantity;
    private BigDecimal pricePerShare;
    private BigDecimal totalAmount; // This is calculated by the service
    private LocalDateTime transactionDate;

    // --- Added for additional notes/details on a transaction ---
    private String notes;
}