package com.stocktrading;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import java.util.Arrays;
import com.stocktrading.repository.StockRepository;
import com.stocktrading.service.StockService;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SpringBootApplication
@EnableScheduling
public class StockTradingApplication {

    // <--- YOU ARE MISSING THIS LINE! ADD IT HERE.
    private static final Logger logger = LoggerFactory.getLogger(StockTradingApplication.class);

    public static void main(String[] args) {
        SpringApplication.run(StockTradingApplication.class, args);
    }

    @Bean
    public CommandLineRunner initDatabase(StockService stockService, StockRepository stockRepository) {
        return args -> {
            List<String> initialSymbols = Arrays.asList("IBM", "MSFT", "AAPL");

            // You can use the logger here now!
            logger.info("--- Initializing/Verifying Stock Data in Database ---");
            for (String symbol : initialSymbols) {
                if (!stockRepository.existsBySymbol(symbol)) {
                    logger.info("Stock {} not found in database. Attempting to fetch and save from API...", symbol);
                    try {
                        stockService.getStockBySymbol(symbol);
                        logger.info("Successfully initialized stock: {}", symbol);
                    } catch (Exception e) {
                        logger.error("Failed to initialize stock {}: {}", symbol, e.getMessage());
                    }
                } else {
                    logger.info("Stock {} already exists in database. Skipping initialization.", symbol);
                }
            }
            logger.info("--- Stock Data Initialization Complete ---");
        };
    }
}