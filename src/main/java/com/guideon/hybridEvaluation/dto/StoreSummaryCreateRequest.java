package com.guideon.hybridEvaluation.dto;

import lombok.Data;

import java.math.BigDecimal;
@Data
public class StoreSummaryCreateRequest {
    private Long sessionId;
    private Long ownerId;
    private String businessRegistrationNo;
    private Integer currentMonth;
    private String summaryYearMonth;
    
    // 매출 관련 (매출성장성 및 안정성)
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
    
    // ESG 관련
    private BigDecimal electricityUsageKwh;
    private BigDecimal electricityBillAmount;
    private BigDecimal gasUsageM3;
    private BigDecimal waterUsageTon;
    private BigDecimal energyEffApplianceRatio;
    private Boolean participateEnergyEffSupport;
    private Boolean participateHighEffEquipSupport;
    private BigDecimal foodWasteKgPerDay;
    private BigDecimal recycleWasteKgPerDay;
    private Boolean yellowUmbrellaMember;
    private Integer yellowUmbrellaMonths;
    private BigDecimal yellowUmbrellaAmount;
    private Integer employmentInsuranceEmployees;
    private BigDecimal customerReviewAvgRating;
    private BigDecimal customerReviewPositiveRatio;
    private Boolean hygieneCertified;
    private Integer originPriceViolationCount;
    
    // 재무 관련
    private BigDecimal operatingProfit;
    private BigDecimal costOfGoodsSold;
    private BigDecimal totalSalary;
    private BigDecimal operatingExpenses;
    private BigDecimal rentExpense;
    private BigDecimal otherExpenses;
    private BigDecimal operatingProfitRatio;
    private BigDecimal cogsRatio;
    private BigDecimal salaryRatio;
    private BigDecimal rentRatio;
    private BigDecimal operatingExpenseRatio;
    
    // 현금흐름 건전성 관련
    private BigDecimal cashPaymentRatioDetail;
    private BigDecimal cardPaymentRatioDetail;
    private BigDecimal otherPaymentRatio;
    private BigDecimal weightedAvgCashPeriod;
    private BigDecimal cashflowCv;
    private BigDecimal avgAccountBalance;
    private BigDecimal minBalanceMaintenanceRatio;
    private BigDecimal excessiveWithdrawalFrequency;
    private BigDecimal rentPaymentComplianceRate;
    private BigDecimal utilityPaymentComplianceRate;
    private BigDecimal salaryPaymentRegularity;
    private BigDecimal taxPaymentIntegrity;
}