package com.stocktrading.repository;

import com.stocktrading.model.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List; // Import List
import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findBySymbol(String symbol);
    boolean existsBySymbol(String symbol);

    // --- NEW METHOD ADDED ---
    // This method allows searching for stocks by symbol or company name, case-insensitively.
    List<Stock> findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(String symbolQuery, String nameQuery);
}