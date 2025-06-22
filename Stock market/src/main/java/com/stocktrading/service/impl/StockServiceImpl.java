package com.stocktrading.service.impl;

import com.stocktrading.dto.StockDTO;
import com.stocktrading.exception.ResourceNotFoundException;
import com.stocktrading.model.Stock;
import com.stocktrading.repository.StockRepository;
import com.stocktrading.service.StockApiClient;
import com.stocktrading.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


@Service
@Transactional
public class StockServiceImpl implements StockService {

    @Autowired
    private StockRepository stockRepository;

    @Autowired
    private StockApiClient stockApiClient;

    private static final Logger logger = LoggerFactory.getLogger(StockServiceImpl.class);

    @Override
    public StockDTO createStock(StockDTO stockDTO) {
        if (stockRepository.existsBySymbol(stockDTO.getSymbol())) {
            throw new RuntimeException("Stock with symbol " + stockDTO.getSymbol() + " already exists.");
        }
        Stock stock = new Stock();
        stock.setSymbol(stockDTO.getSymbol());
        stock.setCompanyName(stockDTO.getCompanyName());
        
        stock.setCurrentPrice(stockDTO.getCurrentPrice() != null && stockDTO.getCurrentPrice().compareTo(BigDecimal.ZERO) > 0 ? stockDTO.getCurrentPrice().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        stock.setPreviousClose(stockDTO.getPreviousClose() != null && stockDTO.getPreviousClose().compareTo(BigDecimal.ZERO) > 0 ? stockDTO.getPreviousClose().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        stock.setDayHigh(stockDTO.getDayHigh() != null && stockDTO.getDayHigh().compareTo(BigDecimal.ZERO) > 0 ? stockDTO.getDayHigh().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        stock.setDayLow(stockDTO.getDayLow() != null && stockDTO.getDayLow().compareTo(BigDecimal.ZERO) > 0 ? stockDTO.getDayLow().setScale(4, RoundingMode.HALF_UP) : BigDecimal.ZERO.setScale(4, RoundingMode.HALF_UP));
        
        stock.setVolume(stockDTO.getVolume() != null && stockDTO.getVolume() > 0 ? stockDTO.getVolume() : 0L);

        stock.setLastUpdated(LocalDateTime.now());

        stock = stockRepository.save(stock);
        logger.info("Created new stock: {}", stock.getSymbol());
        return convertToDTO(stock);
    }

    @Override
    @Transactional(readOnly = true)
    public StockDTO getStockById(Long id) {
        Stock stock = stockRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with ID: " + id));
        return convertToDTO(stock);
    }

    @Override
    @Transactional
    public StockDTO getStockBySymbol(String symbol) {
        Optional<Stock> stockOptional = stockRepository.findBySymbol(symbol);

        if (stockOptional.isPresent()) {
            return convertToDTO(stockOptional.get());
        } else {
            logger.info("Stock {} not found in DB, attempting to fetch from Alpha Vantage API.", symbol);

            Optional<StockApiClient.StockApiData> apiDataOpt = stockApiClient.fetchStockData(symbol);

            if (apiDataOpt.isPresent()) {
                StockApiClient.StockApiData apiData = apiDataOpt.get();

                Optional<String> companyNameOpt = stockApiClient.fetchCompanyName(symbol);

                Stock newStock = new Stock();
                newStock.setSymbol(apiData.getSymbol());
                newStock.setCompanyName(companyNameOpt.orElse("Unknown Company"));
                newStock.setCurrentPrice(apiData.getCurrentPrice().setScale(4, RoundingMode.HALF_UP));
                newStock.setPreviousClose(apiData.getPreviousClose().setScale(4, RoundingMode.HALF_UP));
                newStock.setDayHigh(apiData.getDayHigh().setScale(4, RoundingMode.HALF_UP));
                newStock.setDayLow(apiData.getDayLow().setScale(4, RoundingMode.HALF_UP));
                newStock.setVolume(apiData.getVolume());
                newStock.setLastUpdated(LocalDateTime.now());

                if (newStock.getCurrentPrice() != null && newStock.getPreviousClose() != null && newStock.getPreviousClose().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal changeVal = newStock.getCurrentPrice().subtract(newStock.getPreviousClose());
                    BigDecimal changePercentVal = changeVal.divide(newStock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                                          .multiply(new BigDecimal("100"));
                    newStock.setPriceChange(changeVal);
                    newStock.setChangePercent(changePercentVal);
                } else {
                    newStock.setPriceChange(BigDecimal.ZERO);
                    newStock.setChangePercent(BigDecimal.ZERO);
                }

                Stock savedStock = stockRepository.save(newStock);
                logger.info("Fetched and saved new stock from API: {}", savedStock.getSymbol());
                return convertToDTO(savedStock);
            } else {
                throw new ResourceNotFoundException("Stock not found with symbol: " + symbol + " and could not be fetched from external API.");
            }
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> getAllStocks() {
        return stockRepository.findAll().stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<StockDTO> searchStocks(String query) {
        return stockRepository.findBySymbolContainingIgnoreCaseOrCompanyNameContainingIgnoreCase(query, query).stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    @Override
    public StockDTO updateStockPrice(String symbol, BigDecimal newPrice) {
        Stock stock = stockRepository.findBySymbol(symbol)
                .orElseThrow(() -> new ResourceNotFoundException("Stock not found with symbol: " + symbol));

        if (newPrice == null || newPrice.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("New price must be a positive value.");
        }

        stock.setPreviousClose(stock.getCurrentPrice());
        stock.setCurrentPrice(newPrice.setScale(4, RoundingMode.HALF_UP));
        stock.setLastUpdated(LocalDateTime.now());

        if (stock.getCurrentPrice() != null && stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(BigDecimal.ZERO) != 0) {
            BigDecimal changeVal = stock.getCurrentPrice().subtract(stock.getPreviousClose());
            BigDecimal changePercentVal = changeVal.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                                  .multiply(new BigDecimal("100"));
            stock.setPriceChange(changeVal);
            stock.setChangePercent(changePercentVal);
        } else {
            stock.setPriceChange(BigDecimal.ZERO);
            stock.setChangePercent(BigDecimal.ZERO);
        }

        stock = stockRepository.save(stock);
        logger.info("Stock price updated for {}: New price {}", symbol, newPrice);
        return convertToDTO(stock);
    }

    @Scheduled(fixedRate = 900000)
    public void refreshStockPrices() {
        logger.info("Initiating scheduled stock price refresh from external API...");
        List<Stock> allStocks = stockRepository.findAll();

        for (int i = 0; i < allStocks.size(); i++) {
            Stock stock = allStocks.get(i);
            Optional<BigDecimal> newPriceOpt = stockApiClient.fetchCurrentPrice(stock.getSymbol());

            if (newPriceOpt.isPresent() && newPriceOpt.get().compareTo(BigDecimal.ZERO) > 0) {
                BigDecimal newPrice = newPriceOpt.get();
                stock.setPreviousClose(stock.getCurrentPrice());
                stock.setCurrentPrice(newPrice.setScale(4, RoundingMode.HALF_UP));
                stock.setLastUpdated(LocalDateTime.now());

                if (stock.getCurrentPrice() != null && stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(BigDecimal.ZERO) != 0) {
                    BigDecimal changeVal = stock.getCurrentPrice().subtract(stock.getPreviousClose());
                    BigDecimal changePercentVal = changeVal.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                                      .multiply(new BigDecimal("100"));
                    stock.setPriceChange(changeVal);
                    stock.setChangePercent(changePercentVal);
                } else {
                    stock.setPriceChange(BigDecimal.ZERO);
                    stock.setChangePercent(BigDecimal.ZERO);
                }

                stockRepository.save(stock);
                logger.debug("Refreshed price for {}: New Price {}", stock.getSymbol(), newPrice);
            } else {
                logger.warn("Could not refresh price for {}. Using old price. API response might have been empty or erroneous.", stock.getSymbol());
            }

            if (allStocks.size() > 5 && i < allStocks.size() - 1) {
                try {
                    logger.debug("Pausing for 13 seconds to respect API rate limits...");
                    Thread.sleep(13000);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error("Stock price refresh thread interrupted.", e);
                    return;
                }
            }
        }
        logger.info("Scheduled stock price refresh completed.");
    }

    private StockDTO convertToDTO(Stock stock) {
        StockDTO dto = new StockDTO();
        dto.setId(stock.getId());
        dto.setSymbol(stock.getSymbol());
        dto.setCompanyName(stock.getCompanyName());
        dto.setCurrentPrice(stock.getCurrentPrice());
        dto.setPreviousClose(stock.getPreviousClose());
        dto.setDayHigh(stock.getDayHigh());
        dto.setDayLow(stock.getDayLow());
        dto.setVolume(stock.getVolume());
        dto.setLastUpdated(stock.getLastUpdated());

        // --- THE FIX IS HERE ---
        // Changed dto.setChange() to dto.setPriceChange()
        dto.setPriceChange(stock.getPriceChange());
        dto.setChangePercent(stock.getChangePercent());

        // This fallback logic handles cases where change/changePercent might not have been set on the entity yet
        if (dto.getPriceChange() == null || dto.getChangePercent() == null) { // Check the DTO's new field name
            if (stock.getCurrentPrice() != null && stock.getPreviousClose() != null && stock.getPreviousClose().compareTo(BigDecimal.ZERO) != 0) {
                BigDecimal changeVal = stock.getCurrentPrice().subtract(stock.getPreviousClose());
                dto.setPriceChange(changeVal.setScale(2, RoundingMode.HALF_UP)); // Set the DTO's new field name

                BigDecimal changePercentVal = changeVal.divide(stock.getPreviousClose(), 4, RoundingMode.HALF_UP)
                                                       .multiply(BigDecimal.valueOf(100));
                dto.setChangePercent(changePercentVal.setScale(2, RoundingMode.HALF_UP));
            } else {
                dto.setPriceChange(BigDecimal.ZERO); // Set the DTO's new field name
                dto.setChangePercent(BigDecimal.ZERO);
            }
        }
        return dto;
    }
}