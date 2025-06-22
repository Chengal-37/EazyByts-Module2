package com.stocktrading.service;

import com.stocktrading.exception.InsufficientFundsException;
import com.stocktrading.exception.InsufficientSharesException;
import com.stocktrading.exception.StockNotFoundException;
import com.stocktrading.exception.UserNotFoundException;
import com.stocktrading.payload.response.TransactionHistoryResponse; // NEW IMPORT

import java.util.List; // NEW IMPORT

public interface TradeService {
    /**
     * Executes a stock purchase for a given user.
     * @param userId The ID of the user buying the stock.
     * @param stockSymbol The symbol of the stock to buy.
     * @param quantity The number of shares to buy.
     * @throws UserNotFoundException If the user is not found.
     * @throws StockNotFoundException If the stock is not found.
     * @throws InsufficientFundsException If the user does not have enough funds.
     * @throws IllegalArgumentException If the quantity is invalid (e.g., negative or zero).
     */
    void buyStock(Long userId, String stockSymbol, Integer quantity);

    /**
     * Executes a stock sale for a given user.
     * @param userId The ID of the user selling the stock.
     * @param stockSymbol The symbol of the stock to sell.
     * @param quantity The number of shares to sell.
     * @throws UserNotFoundException If the user is not found.
     * @throws StockNotFoundException If the stock is not found.
     * @throws InsufficientSharesException If the user does not have enough shares to sell.
     * @throws IllegalArgumentException If the quantity is invalid (e.g., negative or zero).
     */
    void sellStock(Long userId, String stockSymbol, Integer quantity);

    /**
     * Retrieves the transaction history for a given user.
     * @param userId The ID of the user.
     * @return A list of TransactionHistoryResponse DTOs.
     * @throws UserNotFoundException If the user is not found.
     */
    List<TransactionHistoryResponse> getTransactionHistory(Long userId); // NEW METHOD
}