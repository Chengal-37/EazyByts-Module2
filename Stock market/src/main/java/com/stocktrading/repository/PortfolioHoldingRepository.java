package com.stocktrading.repository;

import com.stocktrading.model.PortfolioHolding; // Using the new entity name
import com.stocktrading.model.Stock;
import com.stocktrading.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
// Renamed the interface to match the PortfolioHolding entity
public interface PortfolioHoldingRepository extends JpaRepository<PortfolioHolding, Long> {
    List<PortfolioHolding> findByUser(User user);
    Optional<PortfolioHolding> findByUserAndStock(User user, Stock stock);
    void deleteByUserAndStockId(User user, Long stockId); // Good for when shares go to 0
}