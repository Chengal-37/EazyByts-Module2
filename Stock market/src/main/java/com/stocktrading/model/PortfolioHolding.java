package com.stocktrading.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Explicitly add this for JPA, if custom constructors are added
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data // Lombok: Generates getters, setters, toString, equals, hashCode
@NoArgsConstructor // Lombok: Generates a no-argument constructor (essential for JPA)
@Entity
@Table(name = "portfolio_holdings", // Renamed table for clarity
       uniqueConstraints = {
           @UniqueConstraint(columnNames = {"user_id", "stock_id"}) // Ensures a user holds each stock only once
       })
public class PortfolioHolding {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetch for better performance
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // The user who owns this holding

    @ManyToOne(fetch = FetchType.LAZY) // Use LAZY fetch for better performance
    @JoinColumn(name = "stock_id", nullable = false)
    private Stock stock; // The specific stock being held

    @Column(name = "shares", nullable = false) // Renamed from 'quantity' to 'shares' for clarity
    private Integer shares;

    @Column(name = "average_buy_price", precision = 19, scale = 2, nullable = false)
    private BigDecimal averageBuyPrice;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    // --- Constructor for easier creation of new holdings ---
    public PortfolioHolding(User user, Stock stock, Integer shares, BigDecimal averageBuyPrice) {
        this.user = user;
        this.stock = stock;
        this.shares = shares;
        this.averageBuyPrice = averageBuyPrice;
        // Timestamps will be set by @PrePersist on creation
    }

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}