// com.stocktrading.controller.StockController.java

package com.stocktrading.controller;

import com.stocktrading.dto.StockDTO;
import com.stocktrading.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
// Remove @CrossOrigin import

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/stocks")
// REMOVE THIS LINE: @CrossOrigin(origins = "http://localhost:3000")
public class StockController {

    @Autowired
    private StockService stockService;

    @PostMapping
    public ResponseEntity<StockDTO> createStock(@RequestBody StockDTO stockDTO) {
        StockDTO createdStock = stockService.createStock(stockDTO);
        return new ResponseEntity<>(createdStock, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<StockDTO> getStockById(@PathVariable Long id) {
        StockDTO stockDTO = stockService.getStockById(id);
        return ResponseEntity.ok(stockDTO);
    }

    @GetMapping("/symbol/{symbol}")
    public ResponseEntity<StockDTO> getStockBySymbol(@PathVariable String symbol) {
        StockDTO stockDTO = stockService.getStockBySymbol(symbol);
        return ResponseEntity.ok(stockDTO);
    }

    @GetMapping // This is now the ONLY mapping for GET /api/stocks
    public ResponseEntity<List<StockDTO>> getAllStocks() {
        List<StockDTO> stocks = stockService.getAllStocks();
        return ResponseEntity.ok(stocks);
    }

    @GetMapping("/search") // This is now the ONLY mapping for GET /api/stocks/search
    public ResponseEntity<List<StockDTO>> searchStocks(@RequestParam String query) {
        List<StockDTO> stocks = stockService.searchStocks(query);
        return ResponseEntity.ok(stocks);
    }

    @PutMapping("/{symbol}/price")
    public ResponseEntity<StockDTO> updateStockPrice(@PathVariable String symbol,
                                                     @RequestParam BigDecimal newPrice) {
        StockDTO updatedStock = stockService.updateStockPrice(symbol, newPrice);
        return ResponseEntity.ok(updatedStock);
    }

    @PostMapping("/refresh-prices")
    public ResponseEntity<String> refreshStockPrices() {
        stockService.refreshStockPrices();
        return ResponseEntity.ok("Stock prices refresh initiated.");
    }
}