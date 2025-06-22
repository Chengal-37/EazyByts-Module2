package com.stocktrading.payload.response;

import java.math.BigDecimal;
import java.util.List;

public class DashboardResponse {
    private BigDecimal totalValue;
    private PerformanceData performance;

    // Constructor
    public DashboardResponse(BigDecimal totalValue, PerformanceData performance) {
        this.totalValue = totalValue;
        this.performance = performance;
    }

    // Getters and Setters
    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public PerformanceData getPerformance() {
        return performance;
    }

    public void setPerformance(PerformanceData performance) {
        this.performance = performance;
    }

    // Inner class for performance data, as expected by your frontend Chart.js
    public static class PerformanceData {
        private List<String> labels;
        private List<BigDecimal> values; // Use BigDecimal for financial values

        // Constructor
        public PerformanceData(List<String> labels, List<BigDecimal> values) {
            this.labels = labels;
            this.values = values;
        }

        // Getters and Setters
        public List<String> getLabels() {
            return labels;
        }

        public void setLabels(List<String> labels) {
            this.labels = labels;
        }

        public List<BigDecimal> getValues() {
            return values;
        }

        public void setValues(List<BigDecimal> values) {
            this.values = values;
        }
    }
}