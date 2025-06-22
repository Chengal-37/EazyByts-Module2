package com.stocktrading.controller;

import com.stocktrading.payload.request.TradeRequest;
import com.stocktrading.payload.response.MessageResponse; // Make sure this is imported
import com.stocktrading.payload.response.TransactionHistoryResponse; // NEW IMPORT
import com.stocktrading.service.TradeService;
import com.stocktrading.exception.InsufficientFundsException;
import com.stocktrading.exception.InsufficientSharesException;
import com.stocktrading.exception.StockNotFoundException;
import com.stocktrading.exception.UserNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List; // NEW IMPORT

@RestController
@RequestMapping("/api/trades")
public class TradeController {

    @Autowired
    private TradeService tradeService;

    @Autowired
    private com.stocktrading.repository.UserRepository userRepository;

    @PostMapping("/buy")
    public ResponseEntity<?> buyStock(@RequestBody TradeRequest tradeRequest, Principal principal) {
        String username = principal.getName();
        Long userId = userRepository.findByUsername(username)
                                  .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database."))
                                  .getId();

        try {
            tradeService.buyStock(userId, tradeRequest.getStockSymbol(), tradeRequest.getQuantity());
            return ResponseEntity.ok(new MessageResponse("Stock bought successfully!"));
        } catch (InsufficientFundsException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (UserNotFoundException | StockNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    @PostMapping("/sell")
    public ResponseEntity<?> sellStock(@RequestBody TradeRequest tradeRequest, Principal principal) {
        String username = principal.getName();
        Long userId = userRepository.findByUsername(username)
                                  .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database."))
                                  .getId();

        try {
            tradeService.sellStock(userId, tradeRequest.getStockSymbol(), tradeRequest.getQuantity());
            return ResponseEntity.ok(new MessageResponse("Stock sold successfully!"));
        } catch (InsufficientSharesException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new MessageResponse(e.getMessage()));
        } catch (UserNotFoundException | StockNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("An unexpected error occurred: " + e.getMessage()));
        }
    }

    // NEW ENDPOINT FOR TRANSACTION HISTORY
    @GetMapping("/history")
    public ResponseEntity<?> getTransactionHistory(Principal principal) {
        String username = principal.getName();
        Long userId = userRepository.findByUsername(username)
                                  .orElseThrow(() -> new UserNotFoundException("Authenticated user not found in database."))
                                  .getId();

        try {
            List<TransactionHistoryResponse> history = tradeService.getTransactionHistory(userId);
            return ResponseEntity.ok(history); // Return the list of DTOs
        } catch (UserNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new MessageResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new MessageResponse("An unexpected error occurred while fetching transaction history: " + e.getMessage()));
        }
    }
}