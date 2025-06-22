package com.stocktrading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Added for JPA
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor // Lombok annotation for a no-argument constructor, good for JPA
@Entity
@Table(name = "transactions")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Use FetchType.LAZY for ManyToOne relationships to prevent N+1 issues
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    // Use FetchType.LAZY for ManyToOne relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock;

    // Enum for transaction type (BUY or SELL)
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10) // Define a length for STRING enums
    private TransactionType type;

    @Column(nullable = false)
    private Integer quantity;

    // Renamed from pricePerShare to priceAtTrade for consistency with service logic
    @Column(name = "price_at_trade", precision = 19, scale = 4, nullable = false) // Increased scale for precision
    private BigDecimal priceAtTrade;

    // --- REMOVED: total_amount field ---
    // This is a derived value (quantity * priceAtTrade) and should be calculated
    // on the fly in DTOs or services, not stored in the entity to prevent data inconsistencies.
    // @Column(name = "total_amount", precision = 19, scale = 2, nullable = false)
    // private BigDecimal totalAmount;

    // Renamed from transactionDate to timestamp for consistency with service logic
    @Column(nullable = false, updatable = false) // updatable = false means it's set only on creation
    private LocalDateTime timestamp;

    @Column(nullable = true, length = 255) // Notes can be nullable
    private String notes;

    // Sets the timestamp automatically when a new transaction entity is persisted
    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }

    // You can add a constructor for convenience if needed, e.g., for testing or initial setup
    public Transaction(User user, Stock stock, TransactionType type, Integer quantity, BigDecimal priceAtTrade, String notes) {
        this.user = user;
        this.stock = stock;
        this.type = type;
        this.quantity = quantity;
        this.priceAtTrade = priceAtTrade;
        this.notes = notes;
        // timestamp is set by @PrePersist
    }

    // Nested enum for transaction types (matches the DTO)
    public enum TransactionType {
        BUY,
        SELL
    }
}