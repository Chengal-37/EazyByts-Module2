package com.stocktrading.service;

import com.stocktrading.dto.StockDTO;
import java.math.BigDecimal;
import java.util.List;

public interface StockService {
    // --- NEW METHOD ---
    StockDTO createStock(StockDTO stockDTO); // Add this line

    StockDTO getStockById(Long id);
    StockDTO getStockBySymbol(String symbol);
    List<StockDTO> getAllStocks();
    List<StockDTO> searchStocks(String query);
    StockDTO updateStockPrice(String symbol, BigDecimal newPrice);
    void refreshStockPrices();
}