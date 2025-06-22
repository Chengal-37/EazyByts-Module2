package com.stocktrading.service;

import com.stocktrading.dto.TransactionDTO;
import java.time.LocalDateTime;
import java.util.List;

public interface TransactionService {
    TransactionDTO createTransaction(TransactionDTO transactionDTO);
    TransactionDTO getTransactionById(Long id);
    List<TransactionDTO> getUserTransactions(Long userId);
    List<TransactionDTO> getUserTransactionsByDateRange(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    List<TransactionDTO> getUserTransactionsByStock(Long userId, Long stockId);
    void validateTransaction(TransactionDTO transactionDTO);
} 