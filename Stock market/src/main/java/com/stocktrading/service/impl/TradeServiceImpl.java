package com.stocktrading.service.impl;

import com.stocktrading.service.TradeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Autowired;

// Import your entities
import com.stocktrading.model.User;
import com.stocktrading.model.Stock;
import com.stocktrading.model.PortfolioHolding;
import com.stocktrading.model.Transaction;
import java.util.Optional;
import java.util.List; // NEW IMPORT
import java.util.stream.Collectors; // NEW IMPORT

// Import your repositories
import com.stocktrading.repository.UserRepository;
import com.stocktrading.repository.StockRepository;
import com.stocktrading.repository.PortfolioHoldingRepository;
import com.stocktrading.repository.TransactionRepository;

import com.stocktrading.exception.InsufficientFundsException;
import com.stocktrading.exception.InsufficientSharesException;
import com.stocktrading.exception.StockNotFoundException;
import com.stocktrading.exception.UserNotFoundException;
import com.stocktrading.payload.response.TransactionHistoryResponse; // NEW IMPORT

import java.math.BigDecimal;
import java.math.RoundingMode;

@Service
public class TradeServiceImpl implements TradeService {

    private static final Logger logger = LoggerFactory.getLogger(TradeServiceImpl.class);

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private StockRepository stockRepository;
    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;
    @Autowired
    private TransactionRepository transactionRepository;

    @Override
    @Transactional
    public void buyStock(Long userId, String stockSymbol, Integer quantity) {
        logger.info("Attempting to execute BUY trade for user ID: {} for stock: {} quantity: {}", userId, stockSymbol, quantity);

        if (quantity == null || quantity <= 0) {
            logger.warn("Trade failed for user ID {}: Invalid quantity ({}). Quantity must be positive.", userId, quantity);
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + stockSymbol));

        BigDecimal currentPrice = stock.getCurrentPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("BUY Trade failed: Invalid current price for stock {}. Price: {}", stockSymbol, currentPrice);
            throw new RuntimeException("Cannot buy stock with invalid or zero price.");
        }
        BigDecimal totalCost = currentPrice.multiply(BigDecimal.valueOf(quantity));

        if (user.getAccountBalance().compareTo(totalCost) < 0) {
            logger.warn("BUY Trade failed for user ID {}: Insufficient funds. Required: {}, Available: {}",
                        userId, totalCost.setScale(2, RoundingMode.HALF_UP), user.getAccountBalance().setScale(2, RoundingMode.HALF_UP));
            throw new InsufficientFundsException("Insufficient funds to complete the purchase. Required: ₹" +
                                                totalCost.setScale(2, RoundingMode.HALF_UP) + ", Available: ₹" +
                                                user.getAccountBalance().setScale(2, RoundingMode.HALF_UP));
        }

        Optional<PortfolioHolding> existingHolding = portfolioHoldingRepository.findByUserAndStock(user, stock);
        PortfolioHolding portfolioHolding;

        if (existingHolding.isPresent()) {
            portfolioHolding = existingHolding.get();
            BigDecimal oldTotalValue = portfolioHolding.getAverageBuyPrice().multiply(BigDecimal.valueOf(portfolioHolding.getShares()));
            BigDecimal newTotalValue = oldTotalValue.add(totalCost);
            Integer newTotalShares = portfolioHolding.getShares() + quantity;

            if (newTotalShares == 0) {
                logger.error("Error calculating average buy price: newTotalShares is zero for existing holding. User ID: {}, Stock: {}", userId, stockSymbol);
                throw new RuntimeException("Invalid share quantity during portfolio update.");
            }
            portfolioHolding.setAverageBuyPrice(newTotalValue.divide(BigDecimal.valueOf(newTotalShares), 2, RoundingMode.HALF_UP));
            portfolioHolding.setShares(newTotalShares);
        } else {
            portfolioHolding = new PortfolioHolding();
            portfolioHolding.setUser(user);
            portfolioHolding.setStock(stock);
            portfolioHolding.setShares(quantity);
            portfolioHolding.setAverageBuyPrice(currentPrice);
        }
        portfolioHoldingRepository.save(portfolioHolding);

        user.setAccountBalance(user.getAccountBalance().subtract(totalCost));
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(Transaction.TransactionType.BUY);
        transaction.setQuantity(quantity);
        transaction.setPriceAtTrade(currentPrice);
        transactionRepository.save(transaction);

        logger.info("BUY Trade successfully executed for user ID: {} - Bought {} shares of {} for ₹{}",
                    userId, quantity, stockSymbol, totalCost.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    @Transactional
    public void sellStock(Long userId, String stockSymbol, Integer quantity) {
        logger.info("Attempting to execute SELL trade for user ID: {} for stock: {} quantity: {}", userId, stockSymbol, quantity);

        if (quantity == null || quantity <= 0) {
            logger.warn("SELL Trade failed for user ID {}: Invalid quantity ({}). Quantity must be positive.", userId, quantity);
            throw new IllegalArgumentException("Quantity must be positive.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        Stock stock = stockRepository.findBySymbol(stockSymbol)
                .orElseThrow(() -> new StockNotFoundException("Stock not found with symbol: " + stockSymbol));

        Optional<PortfolioHolding> existingHolding = portfolioHoldingRepository.findByUserAndStock(user, stock);

        if (!existingHolding.isPresent()) {
            logger.warn("SELL Trade failed for user ID {}: No holding found for stock {}.", userId, stockSymbol);
            throw new InsufficientSharesException("You do not own shares of " + stockSymbol + " to sell.");
        }

        PortfolioHolding portfolioHolding = existingHolding.get();

        if (portfolioHolding.getShares() < quantity) {
            logger.warn("SELL Trade failed for user ID {}: Insufficient shares. Has: {}, Tries to sell: {}",
                        userId, portfolioHolding.getShares(), quantity);
            throw new InsufficientSharesException("Insufficient shares to sell. You only own " +
                                                 portfolioHolding.getShares() + " shares of " + stockSymbol + ".");
        }

        BigDecimal currentPrice = stock.getCurrentPrice();
        if (currentPrice == null || currentPrice.compareTo(BigDecimal.ZERO) <= 0) {
            logger.warn("SELL Trade failed: Invalid current price for stock {}. Price: {}", stockSymbol, currentPrice);
            throw new RuntimeException("Cannot sell stock with invalid or zero price.");
        }
        BigDecimal totalSaleAmount = currentPrice.multiply(BigDecimal.valueOf(quantity));

        Integer remainingShares = portfolioHolding.getShares() - quantity;
        if (remainingShares == 0) {
            portfolioHoldingRepository.delete(portfolioHolding);
            logger.info("Portfolio holding deleted for user ID {} for stock {}", userId, stockSymbol);
        } else {
            portfolioHolding.setShares(remainingShares);
            portfolioHoldingRepository.save(portfolioHolding);
            logger.info("Portfolio holding updated for user ID {} for stock {}. Remaining shares: {}", userId, stockSymbol, remainingShares);
        }

        user.setAccountBalance(user.getAccountBalance().add(totalSaleAmount));
        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        transaction.setType(Transaction.TransactionType.SELL);
        transaction.setQuantity(quantity);
        transaction.setPriceAtTrade(currentPrice);
        transactionRepository.save(transaction);

        logger.info("SELL Trade successfully executed for user ID: {} - Sold {} shares of {} for ₹{}",
                    userId, quantity, stockSymbol, totalSaleAmount.setScale(2, RoundingMode.HALF_UP));
    }

    @Override
    public List<TransactionHistoryResponse> getTransactionHistory(Long userId) { // NEW METHOD IMPLEMENTATION
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + userId));

        List<Transaction> transactions = transactionRepository.findByUserOrderByTimestampDesc(user); // Assuming this method exists or you create it in TransactionRepository

        return transactions.stream().map(transaction -> new TransactionHistoryResponse(
                transaction.getId(),
                transaction.getStock().getSymbol(),
                transaction.getStock().getCompanyName(),
                transaction.getType().name(), // Enum to String
                transaction.getQuantity(),
                transaction.getPriceAtTrade(),
                transaction.getTimestamp(),
                transaction.getPriceAtTrade().multiply(BigDecimal.valueOf(transaction.getQuantity()))
        )).collect(Collectors.toList());
    }
}