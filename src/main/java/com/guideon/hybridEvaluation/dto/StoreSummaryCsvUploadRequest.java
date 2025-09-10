package com.guideon.hybridEvaluation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class StoreSummaryCsvUploadRequest {
    private Long sessionId;
    private String summaryYearMonth;
    private List<SalesDataRow> salesData;
    
    @Data
    public static class SalesDataRow {
        private BigDecimal totalSalesAmount;
        private BigDecimal weekdaySalesAmount;
        private BigDecimal weekendSalesAmount;
        private BigDecimal lunchSalesRatio;
        private BigDecimal dinnerSalesRatio;
        private Integer transactionCount;
        private Integer weekdayTransactionCount;
        private Integer weekendTransactionCount;
        private BigDecimal momGrowthRate;
        private BigDecimal yoyGrowthRate;
        private BigDecimal salesCv;
        private BigDecimal avgTransactionValue;
        private BigDecimal weekdayAvgTransactionValue;
        private BigDecimal weekendAvgTransactionValue;
        private BigDecimal cashPaymentRatio;
        private BigDecimal cardPaymentRatio;
        private BigDecimal revisitCustomerSalesRatio;
        private BigDecimal newCustomerRatio;
    }
}