package com.stocktrading.service.impl;

import com.stocktrading.dto.PortfolioDTO;
import com.stocktrading.dto.PortfolioItemDTO;
import com.stocktrading.dto.StockDTO;
import com.stocktrading.model.PortfolioHolding; // Changed import
import com.stocktrading.model.Stock;
import com.stocktrading.model.User;
import com.stocktrading.repository.PortfolioHoldingRepository; // Changed import
import com.stocktrading.repository.UserRepository;
import com.stocktrading.service.PortfolioService;
import com.stocktrading.service.StockService; // Assumed service for fetching live/cached stock data
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional; // Added import
import java.util.stream.Collectors;

@Service
@Transactional // Ensures methods are executed within a transaction context
public class PortfolioServiceImpl implements PortfolioService {

    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository; // Renamed repository

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockService stockService; // Service to get current stock prices

    // Use a logger for better debugging and insights
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(PortfolioServiceImpl.class);

    @Override
    public PortfolioDTO getUserPortfolio(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        // Get all portfolio holdings for the user
        List<PortfolioHolding> holdings = portfolioHoldingRepository.findByUser(user);

        // Convert holdings to DTOs and calculate dynamic values
        return convertToPortfolioDTO(holdings, user);
    }

    @Override
    public PortfolioDTO getPortfolioById(Long portfolioHoldingId) { // Renamed parameter for clarity
        // This method likely refers to a single PortfolioHolding by its ID,
        // but the return type is PortfolioDTO (representing all holdings for a user).
        // Let's assume it should return the entire portfolio for the user who owns that holding.
        PortfolioHolding holding = portfolioHoldingRepository.findById(portfolioHoldingId)
                .orElseThrow(() -> new RuntimeException("Portfolio holding not found with ID: " + portfolioHoldingId));

        List<PortfolioHolding> userHoldings = portfolioHoldingRepository.findByUser(holding.getUser());
        return convertToPortfolioDTO(userHoldings, holding.getUser());
    }

    @Override
    public List<PortfolioDTO> getAllPortfolios() {
        // This method fetches all holdings from the database, groups them by user,
        // and converts each user's holdings into a PortfolioDTO.
        return portfolioHoldingRepository.findAll().stream()
                .collect(Collectors.groupingBy(PortfolioHolding::getUser)) // Group by User
                .entrySet().stream() // Get map entries (User -> List<PortfolioHolding>)
                .map(entry -> convertToPortfolioDTO(entry.getValue(), entry.getKey())) // Convert each group to PortfolioDTO
                .collect(Collectors.toList());
    }

    // --- Methods for updating values are removed/repurposed ---
    // The previous updatePortfolioValue and refreshAllPortfolios were trying to persist
    // 'currentValue' and 'totalInvestment' directly into the PortfolioHolding entity.
    // Since these are now calculated dynamically for DTOs, these methods are no longer needed
    // in their previous form. The 'StockService' should be responsible for updating Stock prices.

    // If you need to trigger a refresh of stock prices from an external API,
    // that logic belongs in StockService, possibly using Spring's @Scheduled annotation.
    // For example, StockService might have a method like:
    // @Scheduled(fixedRate = 60000) // Every 1 minute
    // public void refreshMarketStockPrices() { ... }


    // --- Helper method to convert List<PortfolioHolding> to PortfolioDTO ---
    private PortfolioDTO convertToPortfolioDTO(List<PortfolioHolding> holdings, User user) {
        PortfolioDTO dto = new PortfolioDTO();
        dto.setUserId(user.getId());
        dto.setCashBalance(user.getAccountBalance()); // Include user's cash balance

        // Convert individual holdings to PortfolioItemDTOs
        List<PortfolioItemDTO> holdingItems = holdings.stream()
                .map(this::convertToPortfolioItemDTO)
                .filter(Optional::isPresent) // Filter out any holdings that couldn't be converted (e.g., stock price not found)
                .map(Optional::get)
                .collect(Collectors.toList());

        dto.setHoldings(holdingItems);

        // Calculate totals for the entire portfolio
        BigDecimal totalCurrentValue = BigDecimal.ZERO;
        BigDecimal totalHistoricalInvestment = BigDecimal.ZERO; // Renamed to avoid confusion with entity field

        for (PortfolioItemDTO item : holdingItems) {
            totalCurrentValue = totalCurrentValue.add(item.getTotalValue());
            totalHistoricalInvestment = totalHistoricalInvestment.add(item.getAverageBuyPrice().multiply(BigDecimal.valueOf(item.getShares()))); // Use item.getShares()
        }

        dto.setTotalValue(totalCurrentValue);
        dto.setTotalInvestment(totalHistoricalInvestment);

        // Calculate total gain/loss and percentage for the portfolio
        BigDecimal totalGainLoss = totalCurrentValue.subtract(totalHistoricalInvestment);
        dto.setTotalGainLoss(totalGainLoss);

        if (totalHistoricalInvestment.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal totalGainLossPercent = totalGainLoss
                    .divide(totalHistoricalInvestment, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dto.setTotalGainLossPercent(totalGainLossPercent);
        } else {
            dto.setTotalGainLossPercent(BigDecimal.ZERO);
        }

        return dto;
    }

    // --- Helper method to convert PortfolioHolding to PortfolioItemDTO ---
    // Returns Optional<PortfolioItemDTO> in case stock data is not found
    private Optional<PortfolioItemDTO> convertToPortfolioItemDTO(PortfolioHolding holding) {
        PortfolioItemDTO dto = new PortfolioItemDTO();
        Stock stock = holding.getStock(); // Get the associated Stock entity

        // Attempt to get the latest stock price from StockService
        // It's crucial that stockService.getStockBySymbol fetches the *latest* price,
        // either from its cache (the Stock entity) or by calling an external API.
        Optional<StockDTO> currentStockDataOptional = Optional.empty();
        try {
             currentStockDataOptional = Optional.ofNullable(stockService.getStockBySymbol(stock.getSymbol()));
        } catch (Exception e) {
            logger.error("Could not fetch current stock data for symbol {}: {}", stock.getSymbol(), e.getMessage());
            // If stock data cannot be fetched, we can choose to skip this item or use stale data
            // For now, let's return empty optional, meaning this holding won't appear
            return Optional.empty();
        }

        if (currentStockDataOptional.isEmpty()) {
            logger.warn("No current stock data found for symbol: {}", stock.getSymbol());
            return Optional.empty(); // Cannot calculate if no current price
        }

        StockDTO currentStockData = currentStockDataOptional.get();
        BigDecimal currentPrice = currentStockData.getCurrentPrice();

        if (currentPrice == null) { // Defensive check
            logger.warn("Current price is null for symbol: {}", stock.getSymbol());
            return Optional.empty();
        }

        // Map fields
        dto.setHoldingId(holding.getId()); // Set the ID of the portfolio holding
        dto.setStockId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setCompanyName(stock.getCompanyName());
        dto.setShares(holding.getShares()); // Renamed from getQuantity()
        dto.setAverageBuyPrice(holding.getAverageBuyPrice());
        dto.setCurrentPrice(currentPrice); // Set dynamically fetched current price

        // Calculate dynamic values
        BigDecimal totalValue = currentPrice.multiply(BigDecimal.valueOf(holding.getShares()));
        dto.setTotalValue(totalValue);

        BigDecimal historicalInvestmentForHolding = holding.getAverageBuyPrice().multiply(BigDecimal.valueOf(holding.getShares()));
        BigDecimal gainLoss = totalValue.subtract(historicalInvestmentForHolding);
        dto.setGainLoss(gainLoss);

        if (historicalInvestmentForHolding.compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal gainLossPercent = gainLoss
                    .divide(historicalInvestmentForHolding, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100));
            dto.setGainLossPercent(gainLossPercent);
        } else {
            dto.setGainLossPercent(BigDecimal.ZERO);
        }

        return Optional.of(dto);
    }
}