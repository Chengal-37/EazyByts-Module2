// com.stocktrading.controller.MarketController.java

package com.stocktrading.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.Map;
import java.util.HashMap;
// Remove @CrossOrigin import
// Remove @RequestParam import

@RestController
@RequestMapping("/api")
// REMOVE THIS LINE: @CrossOrigin(origins = "http://127.0.0.1:5500", maxAge = 3600)
public class MarketController {

    @GetMapping("/market-data")
    public ResponseEntity<?> getMarketData() {
        Map<String, Object> marketData = new HashMap<>();
        marketData.put("nifty", 22500.50);
        marketData.put("sensex", 75000.75);
        marketData.put("lastUpdated", "2025-06-19T12:00:00Z");
        marketData.put("marketStatus", "Open");
        return ResponseEntity.ok(marketData);
    }

    // REMOVED METHOD: getAvailableStocks()
    // REMOVED METHOD: searchStocks()
}