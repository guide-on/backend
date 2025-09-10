package com.guideon.hybridEvaluation.domain;

import lombok.Data;

import java.math.BigDecimal;
import java.sql.Timestamp;

@Data
public class StoreSummary {
    private Long sessionId;  // 세션 식별자
    private Long ownerId;   // 사업주 ID
    private String businessRegistrationNo;  // 사업자등록번호
    private Integer currentMonth;  // 영업개월수
    private String summaryYearMonth;  // 요약 기준 년월 (예: 2025-08)
    
    // 매출 관련 (매출성장성 및 안정성)
    private BigDecimal totalSalesAmount;  // 월별 매출액 평균
    private BigDecimal weekdaySalesAmount;  // 월별 주중 매출 평균
    private BigDecimal weekendSalesAmount;  // 월별 주말 매출 평균
    private BigDecimal lunchSalesRatio;  // 점심시간 매출 비중 (11-14시)
    private BigDecimal dinnerSalesRatio;  // 저녁시간 매출 비중 (18-21시)
    private Integer transactionCount;  // 월 총 결제건수
    private Integer weekdayTransactionCount;  // 주중 결제건수
    private Integer weekendTransactionCount;  // 주말 결제건수
    private BigDecimal momGrowthRate;  // 전월 대비 매출 성장률 (%)
    private BigDecimal yoyGrowthRate;  // 전년 동월 대비 성장률 (%)
    private BigDecimal salesCv;  // 매출 변동성 (일별 매출 표준편차/평균)
    private BigDecimal avgTransactionValue;  // 월 평균 객단가 (매출/결제건수)
    private BigDecimal weekdayAvgTransactionValue;  // 주중 객단가
    private BigDecimal weekendAvgTransactionValue;  // 주말 객단가
    private BigDecimal cashPaymentRatio;  // 현금 결제 비율 (%)
    private BigDecimal cardPaymentRatio;  // 카드 결제 비율 (%)
    private BigDecimal revisitCustomerSalesRatio;  // 재방문 고객 매출 비중 (%)
    private BigDecimal newCustomerRatio;  // 신규 고객 비율 (%)
    
    // 생성/수정 일시
    private Timestamp createdDttm;  // 생성일시
    private Timestamp updatedDttm;  // 수정일시
    private Timestamp lastUpdatedDttm;  // 데이터 최종 계산 시점
    
    // ESG 관련
    private BigDecimal electricityUsageKwh;  // 월 전력 사용량 (kWh)
    private BigDecimal electricityBillAmount;  // 월 전기요금 (원)
    private BigDecimal gasUsageM3;  // 월 가스 사용량 (㎥)
    private BigDecimal waterUsageTon;  // 월 수도 사용량 (톤)
    private BigDecimal energyEffApplianceRatio;  // 에너지효율등급 가전 비율 (%)
    private Boolean participateEnergyEffSupport;  // 에너지효율향상 지원사업 참여 여부
    private Boolean participateHighEffEquipSupport;  // 고효율기기 구매 지원사업 참여 여부
    private BigDecimal foodWasteKgPerDay;  // 일일 음식물 쓰레기 배출량 (kg)
    private BigDecimal recycleWasteKgPerDay;  // 일일 재활용품 배출량 (kg)
    private Boolean yellowUmbrellaMember;  // 노란우산 공제 가입 여부
    private Integer yellowUmbrellaMonths;  // 노란우산 공제 납부 개월 수
    private BigDecimal yellowUmbrellaAmount;  // 노란우산 공제 월 납부액 (원)
    private Integer employmentInsuranceEmployees;  // 고용보험 가입 직원 수
    private BigDecimal customerReviewAvgRating;  // 고객 리뷰 평균 평점
    private BigDecimal customerReviewPositiveRatio;  // 고객 리뷰 긍정 비율 (%)
    private Boolean hygieneCertified;  // 위생등급 인증 여부
    private Integer originPriceViolationCount;  // 원산지·가격 표시 위반 횟수
    
    // 재무 관련
    private BigDecimal operatingProfit;  // 월별 영업이익 (원)
    private BigDecimal costOfGoodsSold;  // 월별 매출원가 (원)
    private BigDecimal totalSalary;  // 월별 급여총액 (원)
    private BigDecimal operatingExpenses;  // 월별 영업비용 (원)
    private BigDecimal rentExpense;  // 월별 임차료 (원)
    private BigDecimal otherExpenses;  // 월별 기타비용 (원)
    private BigDecimal operatingProfitRatio;  // 영업이익률 (%)
    private BigDecimal cogsRatio;  // 매출원가율 (%)
    private BigDecimal salaryRatio;  // 급여비율 (%)
    private BigDecimal rentRatio;  // 임차료율 (%)
    private BigDecimal operatingExpenseRatio;  // 영업비용률 (%)
    
    // 현금흐름 건전성 관련
    private BigDecimal cashPaymentRatioDetail;  // 현금 결제 비율 (상세) (%)
    private BigDecimal cardPaymentRatioDetail;  // 카드 결제 비율 (상세) (%)
    private BigDecimal otherPaymentRatio;  // 기타 결제 비율 (%)
    private BigDecimal weightedAvgCashPeriod;  // 가중평균 현금화 기간 (일)
    private BigDecimal cashflowCv;  // 현금흐름 변동계수 (CV)
    private BigDecimal avgAccountBalance;  // 평균 계좌 잔액 (원)
    private BigDecimal minBalanceMaintenanceRatio;  // 최소 잔액 유지 비율 (%)
    private BigDecimal excessiveWithdrawalFrequency;  // 과다 인출 빈도 (월별 횟수)
    private BigDecimal rentPaymentComplianceRate;  // 임대료 납부 준수율 (%)
    private BigDecimal utilityPaymentComplianceRate;  // 공과금 납부 준수율 (%)
    private BigDecimal salaryPaymentRegularity;  // 급여 지급 정상성 (%)
    private BigDecimal taxPaymentIntegrity;  // 세금 납부 성실도 (%)
}