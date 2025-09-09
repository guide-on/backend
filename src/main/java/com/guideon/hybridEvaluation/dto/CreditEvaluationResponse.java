package com.guideon.hybridEvaluation.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class CreditEvaluationResponse {
    // 기본 정보
    private String memberId;  // member_id 컬럼에 대응
    private Timestamp evaluationDate;
    
    // 상환이력 (28.4%)
    private Integer totalOverdueCount;
    private Integer recent12mOverdueCount;
    private Integer maxOverdueDays;
    private BigDecimal currentOverdueAmount;
    private Integer loanDefaultHistory;
    private BigDecimal creditCardDelayRate;
    private Integer paymentConsistencyScore;
    
    // 부채수준 (24.5%)
    private BigDecimal totalDebtAmount;
    private BigDecimal monthlyIncome;
    private BigDecimal debtToIncomeRatio;
    private BigDecimal creditCardUtilizationRate;
    private BigDecimal securedVsUnsecuredRatio;
    
    // 신용거래기간 (12.3%)
    private Integer creditHistoryMonths;
    private Integer oldestCreditAccountMonths;
    private Integer newCreditInquiries6m;
    
    // 신용형태 (27.5%)
    private Integer activeCreditCardCount;
    private BigDecimal totalCreditLimit;
    private Integer loanTypeDiversity;
    private Integer financialInstitutionCount;
    
    // 비금융/마이데이터 (7.3%)
    private Integer alternativeCreditScore;
    
    // 메타데이터
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
