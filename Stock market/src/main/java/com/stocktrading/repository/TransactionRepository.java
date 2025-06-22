package com.stocktrading.repository;

import com.stocktrading.model.Stock;
import com.stocktrading.model.Transaction;
import com.stocktrading.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

    // Find all transactions for a specific user
    List<Transaction> findByUser(User user);

    // --- MODIFIED METHOD ---
    // Changed 'TransactionDate' to 'Timestamp' to match the updated Transaction entity field
    List<Transaction> findByUserAndTimestampBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    // Find all transactions for a specific user and stock
    List<Transaction> findByUserAndStock(User user, Stock stock);

    // --- NEW METHOD ---
    // Added to get all transactions for a user, ordered by timestamp (most recent first)
    List<Transaction> findByUserOrderByTimestampDesc(User user);
}