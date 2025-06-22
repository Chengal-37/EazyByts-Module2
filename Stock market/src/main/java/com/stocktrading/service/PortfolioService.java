package com.stocktrading.service;

import com.stocktrading.dto.PortfolioDTO;
import java.util.List;

public interface PortfolioService {

    PortfolioDTO getUserPortfolio(Long userId);

    PortfolioDTO getPortfolioById(Long portfolioId);

    List<PortfolioDTO> getAllPortfolios();

    // --- IMPORTANT: THESE METHODS ARE REMOVED ---
    // They are no longer part of the service contract because:
    // 1. PortfolioHolding entity no longer stores 'currentValue' or 'totalInvestment'.
    // 2. These values are calculated dynamically in the DTO conversion.
    // 3. The responsibility of updating live stock prices belongs to a StockService.
    // void updatePortfolioValue(Long portfolioId);
    // void refreshAllPortfolios();
}