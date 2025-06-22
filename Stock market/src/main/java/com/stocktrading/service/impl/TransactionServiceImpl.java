package com.stocktrading.service.impl;

import com.stocktrading.dto.TransactionDTO;
// REMOVED: import com.stocktrading.dto.TransactionTypeDTO; // This import is no longer needed
import com.stocktrading.model.PortfolioHolding;
import com.stocktrading.model.Stock;
import com.stocktrading.model.Transaction;
import com.stocktrading.model.Transaction.TransactionType; // CORRECTED: Import model's nested enum directly
import com.stocktrading.model.User;
import com.stocktrading.repository.PortfolioHoldingRepository;
import com.stocktrading.repository.StockRepository;
import com.stocktrading.repository.TransactionRepository;
import com.stocktrading.repository.UserRepository;
import com.stocktrading.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private PortfolioHoldingRepository portfolioHoldingRepository;

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(TransactionServiceImpl.class);

    @Override
    public TransactionDTO createTransaction(TransactionDTO transactionDTO) {
        validateTransaction(transactionDTO);

        User user = userRepository.findById(transactionDTO.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + transactionDTO.getUserId()));

        Stock stock = stockRepository.findById(transactionDTO.getStockId())
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + transactionDTO.getStockId()));

        BigDecimal priceAtTrade = stock.getCurrentPrice();
        if (priceAtTrade == null || priceAtTrade.compareTo(BigDecimal.ZERO) <= 0) {
            logger.error("Attempted to trade stock {} with invalid current price: {}", stock.getSymbol(), priceAtTrade);
            throw new RuntimeException("Cannot trade stock with invalid current price: " + stock.getSymbol());
        }
        
        BigDecimal calculatedTotalAmount = priceAtTrade.multiply(BigDecimal.valueOf(transactionDTO.getQuantity()))
                                            .setScale(2, RoundingMode.HALF_UP);

        // CORRECTED: Use TransactionDTO.TransactionType
        if (transactionDTO.getType() == TransactionDTO.TransactionType.BUY) {
            if (user.getAccountBalance().compareTo(calculatedTotalAmount) < 0) {
                throw new RuntimeException("Insufficient funds for this purchase. Required: " + calculatedTotalAmount + ", Available: " + user.getAccountBalance());
            }
            user.setAccountBalance(user.getAccountBalance().subtract(calculatedTotalAmount));

        // CORRECTED: Use TransactionDTO.TransactionType
        } else if (transactionDTO.getType() == TransactionDTO.TransactionType.SELL) {
            Optional<PortfolioHolding> existingHolding = portfolioHoldingRepository.findByUserAndStock(user, stock);
            if (existingHolding.isEmpty() || existingHolding.get().getShares() < transactionDTO.getQuantity()) {
                throw new RuntimeException("Insufficient shares to sell for stock: " + stock.getSymbol() + ". Available: " + (existingHolding.isPresent() ? existingHolding.get().getShares() : 0));
            }
            user.setAccountBalance(user.getAccountBalance().add(calculatedTotalAmount));

        } else {
            throw new RuntimeException("Invalid transaction type: " + transactionDTO.getType());
        }

        userRepository.save(user);

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setStock(stock);
        // CORRECTED: Map DTO's nested enum to Model's nested enum
        transaction.setType(TransactionType.valueOf(transactionDTO.getType().name()));
        transaction.setQuantity(transactionDTO.getQuantity());
        transaction.setPriceAtTrade(priceAtTrade);
        transaction.setNotes(transactionDTO.getNotes());

        transaction = transactionRepository.save(transaction);
        updatePortfolioHolding(transaction);

        return convertToDTO(transaction);
    }

    @Override
    public TransactionDTO getTransactionById(Long id) {
        Transaction transaction = transactionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Transaction not found with ID: " + id));
        return convertToDTO(transaction);
    }

    @Override
    public List<TransactionDTO> getUserTransactions(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return transactionRepository.findByUserOrderByTimestampDesc(user).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        return transactionRepository.findByUserAndTimestampBetween(user, startDate, endDate).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionDTO> getUserTransactionsByStock(Long userId, Long stockId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with ID: " + userId));

        Stock stock = stockRepository.findById(stockId)
                .orElseThrow(() -> new RuntimeException("Stock not found with ID: " + stockId));

        return transactionRepository.findByUserAndStock(user, stock).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void validateTransaction(TransactionDTO transactionDTO) {
        if (transactionDTO.getQuantity() == null || transactionDTO.getQuantity() <= 0) {
            throw new RuntimeException("Transaction quantity must be greater than 0");
        }
        if (transactionDTO.getPricePerShare() == null || transactionDTO.getPricePerShare().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price per share must be greater than 0");
        }
        if (transactionDTO.getType() == null) {
            throw new RuntimeException("Transaction type must be specified (BUY or SELL)");
        }
    }

    private void updatePortfolioHolding(Transaction transaction) {
        User user = transaction.getUser();
        Stock stock = transaction.getStock();
        int transactionQuantity = transaction.getQuantity();
        BigDecimal transactionPrice = transaction.getPriceAtTrade();
        TransactionType transactionType = transaction.getType(); // This is the model's enum

        Optional<PortfolioHolding> existingHoldingOptional = portfolioHoldingRepository.findByUserAndStock(user, stock);
        PortfolioHolding holding;

        if (transactionType == TransactionType.BUY) { // Using model's enum
            if (existingHoldingOptional.isPresent()) {
                holding = existingHoldingOptional.get();
                int oldShares = holding.getShares();
                BigDecimal oldAveragePrice = holding.getAverageBuyPrice();

                BigDecimal newTotalCost = (oldAveragePrice.multiply(BigDecimal.valueOf(oldShares)))
                                        .add(transactionPrice.multiply(BigDecimal.valueOf(transactionQuantity)));
                
                int newShares = oldShares + transactionQuantity;
                
                BigDecimal newAveragePrice = newTotalCost.divide(BigDecimal.valueOf(newShares), 4, RoundingMode.HALF_UP);

                holding.setShares(newShares);
                holding.setAverageBuyPrice(newAveragePrice);
            } else {
                holding = new PortfolioHolding(user, stock, transactionQuantity, transactionPrice);
            }
            portfolioHoldingRepository.save(holding);

        } else if (transactionType == TransactionType.SELL) { // Using model's enum
            holding = existingHoldingOptional.get();
            
            int newShares = holding.getShares() - transactionQuantity;
            
            if (newShares == 0) {
                portfolioHoldingRepository.delete(holding);
                logger.info("Portfolio holding deleted for user {} stock {} as shares reached 0", user.getId(), stock.getSymbol());
            } else {
                holding.setShares(newShares);
                portfolioHoldingRepository.save(holding);
            }
        }
    }

    private TransactionDTO convertToDTO(Transaction transaction) {
        TransactionDTO dto = new TransactionDTO();
        dto.setId(transaction.getId());
        dto.setUserId(transaction.getUser().getId());
        dto.setStockId(transaction.getStock().getId());
        
        dto.setSymbol(transaction.getStock().getSymbol());
        dto.setCompanyName(transaction.getStock().getCompanyName());

        // CORRECTED: Map Model's nested enum to DTO's nested enum
        dto.setType(TransactionDTO.TransactionType.valueOf(transaction.getType().name()));
        dto.setQuantity(transaction.getQuantity());
        dto.setPricePerShare(transaction.getPriceAtTrade());
        
        BigDecimal totalAmount = transaction.getPriceAtTrade().multiply(BigDecimal.valueOf(transaction.getQuantity()));
        dto.setTotalAmount(totalAmount.setScale(2, RoundingMode.HALF_UP));

        dto.setTransactionDate(transaction.getTimestamp());
        dto.setNotes(transaction.getNotes());
        return dto;
    }
}