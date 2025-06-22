package com.stocktrading.service;

import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.math.BigDecimal;
import java.util.Optional;

import org.slf4j.Logger;         // <--- ADD THIS IMPORT
import org.slf4j.LoggerFactory;  // <--- ADD THIS IMPORT

@Service
public class StockApiClient {

    private static final Logger logger = LoggerFactory.getLogger(StockApiClient.class); // <--- ADD THIS LOGGER DECLARATION

    private final WebClient webClient;

    // Make these 'final' since they are now injected via constructor
    private final String apiBaseUrl;
    private final String apiKey;

    // Constructor: Inject @Value properties directly into the constructor parameters
    public StockApiClient(
        WebClient.Builder webClientBuilder,
        @Value("${stock.api.base-url}") String apiBaseUrl, // <--- INJECTED HERE
        @Value("${stock.api.key}") String apiKey          // <--- INJECTED HERE
    ) {
        // Assign injected values to your class fields
        this.apiBaseUrl = apiBaseUrl;
        this.apiKey = apiKey;

        // Now, apiBaseUrl will have the correct value from application.properties
        this.webClient = webClientBuilder.baseUrl(this.apiBaseUrl).build();

        // <--- DEBUG PRINTS (KEEP THESE FOR VERIFICATION) ---
        logger.info("StockApiClient initialized. Base URL: {}", this.apiBaseUrl);
        logger.info("StockApiClient initialized. API Key: {}", this.apiKey);
        // ----------------------------------------------------
    }

    /**
     * Fetches current stock price for a given symbol from the external API.
     * Uses Alpha Vantage's GLOBAL_QUOTE endpoint.
     * https://www.alphavantage.co/query?function=GLOBAL_QUOTE&symbol=IBM&apikey=YOUR_API_KEY
     *
     * @param symbol The stock symbol (e.g., "IBM", "AAPL").
     * @return An Optional containing the current price as BigDecimal if successful, empty otherwise.
     */
    public Optional<BigDecimal> fetchCurrentPrice(String symbol) {
        String url = "/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;

        try {
            JsonNode response = webClient.get().uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block(); // Synchronously block for the result

            JsonNode globalQuote = response.path("Global Quote");

            if (globalQuote.has("05. price")) {
                return Optional.of(new BigDecimal(globalQuote.get("05. price").asText()));
            } else if (response.has("Error Message")) {
                logger.error("API Error for {}: {}", symbol, response.get("Error Message").asText());
                return Optional.empty();
            } else if (response.has("Note")) {
                logger.warn("API Note (likely rate limit) for {}: {}", symbol, response.get("Note").asText());
                return Optional.empty();
            } else {
                logger.warn("Unexpected API response for {}: {}", symbol, response.toPrettyString());
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch price for {} from API: {}", symbol, e.getMessage(), e); // Log the exception for more detail
            return Optional.empty();
        }
    }

    /**
     * Fetches comprehensive stock data (price, previous close, high, low, volume)
     * for a given symbol. Uses Alpha Vantage's GLOBAL_QUOTE endpoint.
     *
     * IMPORTANT: Alpha Vantage's GLOBAL_QUOTE does NOT provide the full company name.
     * You will need to use fetchCompanyName() separately or rely on pre-existing data.
     *
     * @param symbol The stock symbol.
     * @return An Optional containing StockApiData if successful, empty otherwise.
     */
    public Optional<StockApiData> fetchStockData(String symbol) {
        String url = "/query?function=GLOBAL_QUOTE&symbol=" + symbol + "&apikey=" + apiKey;

        try {
            JsonNode response = webClient.get().uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode globalQuote = response.path("Global Quote");

            if (globalQuote.has("05. price")) {
                StockApiData data = new StockApiData();
                data.setSymbol(globalQuote.path("01. symbol").asText(symbol));
                data.setCurrentPrice(new BigDecimal(globalQuote.get("05. price").asText()));
                data.setPreviousClose(new BigDecimal(globalQuote.get("08. previous close").asText()));
                data.setDayHigh(new BigDecimal(globalQuote.get("03. high").asText()));
                data.setDayLow(new BigDecimal(globalQuote.get("04. low").asText()));
                data.setVolume(globalQuote.get("06. volume").asLong());
                data.setCompanyName(null); // Will be populated by fetchCompanyName later
                return Optional.of(data);
            } else if (response.has("Error Message")) {
                logger.error("API Error for {}: {}", symbol, response.get("Error Message").asText());
                return Optional.empty();
            } else if (response.has("Note")) {
                logger.warn("API Note (likely rate limit) for {}: {}", symbol, response.get("Note").asText());
                return Optional.empty();
            } else {
                logger.warn("Unexpected API response for {}: {}", symbol, response.toPrettyString());
                return Optional.empty();
            }
        } catch (Exception e) {
            logger.error("Failed to fetch comprehensive data for {} from API: {}", symbol, e.getMessage(), e);
            return Optional.empty();
        }
    }

    /**
     * Fetches the company name for a given stock symbol.
     * Uses Alpha Vantage's SYMBOL_SEARCH endpoint to find the best match.
     *
     * @param symbol The stock symbol (e.g., "IBM", "MSFT").
     * @return An Optional containing the company name if found, empty otherwise.
     */
    public Optional<String> fetchCompanyName(String symbol) {
        String url = "/query?function=SYMBOL_SEARCH&keywords=" + symbol + "&apikey=" + apiKey;

        try {
            JsonNode response = webClient.get().uri(url)
                    .retrieve()
                    .bodyToMono(JsonNode.class)
                    .block();

            JsonNode bestMatches = response.path("bestMatches");

            if (bestMatches.isArray()) {
                for (JsonNode match : bestMatches) {
                    if (match.has("1. symbol") && match.get("1. symbol").asText().equalsIgnoreCase(symbol)) {
                        if (match.has("2. name")) {
                            return Optional.of(match.get("2. name").asText());
                        }
                    }
                }
            } else if (response.has("Error Message")) {
                logger.error("API Error for symbol search {}: {}", symbol, response.get("Error Message").asText());
                return Optional.empty();
            } else if (response.has("Note")) {
                logger.warn("API Note (likely rate limit) for symbol search {}: {}", symbol, response.get("Note").asText());
                return Optional.empty();
            }

            logger.warn("Company name not found for {} via SYMBOL_SEARCH or unexpected response: {}", symbol, response.toPrettyString());
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Failed to fetch company name for {} from API: {}", symbol, e.getMessage(), e);
            return Optional.empty();
        }
    }

    // --- Inner DTO to map the external API response ---
    public static class StockApiData {
        private String symbol;
        private String companyName;
        private BigDecimal currentPrice;
        private BigDecimal previousClose;
        private BigDecimal dayHigh;
        private BigDecimal dayLow;
        private Long volume;

        // Getters and Setters
        public String getSymbol() { return symbol; }
        public void setSymbol(String symbol) { this.symbol = symbol; }
        public String getCompanyName() { return companyName; }
        public void setCompanyName(String companyName) { this.companyName = companyName; }
        public BigDecimal getCurrentPrice() { return currentPrice; }
        public void setCurrentPrice(BigDecimal currentPrice) { this.currentPrice = currentPrice; }
        public BigDecimal getPreviousClose() { return previousClose; }
        public void setPreviousClose(BigDecimal previousClose) { this.previousClose = previousClose; }
        public BigDecimal getDayHigh() { return dayHigh; }
        public void setDayHigh(BigDecimal dayHigh) { this.dayHigh = dayHigh; }
        public BigDecimal getDayLow() { return dayLow; }
        public void setDayLow(BigDecimal dayLow) { this.dayLow = dayLow; }
        public Long getVolume() { return volume; }
        public void setVolume(Long volume) { this.volume = volume; }
    }
}