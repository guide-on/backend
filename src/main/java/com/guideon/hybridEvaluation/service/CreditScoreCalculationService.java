package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.domain.CreditEvaluation;
import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
public class CreditScoreCalculationService {
    
    // 점수 경계값
    private static final int MIN_TOTAL_SCORE = 350;
    private static final int MAX_TOTAL_SCORE = 980;
    
    // 각 영역별 최대 점수
    private static final int MAX_REPAYMENT_SCORE = 284;
    private static final int MAX_DEBT_LEVEL_SCORE = 318;  
    private static final int MAX_CREDIT_PERIOD_SCORE = 123;
    private static final int MAX_CREDIT_PATTERN_SCORE = 275;
    private static final int MAX_NON_FINANCIAL_SCORE = 0; // 비금융 점수는 별도 구현 필요
    
    /**
     * 신용평가 데이터를 기반으로 신용점수 결과를 계산
     * @param evaluation 신용평가 데이터
     * @return 계산된 신용평가 결과
     */
    public CreditEvaluationResult calculateCreditScore(CreditEvaluation evaluation) {
        log.info("신용점수 계산 시작: sessionId={}", evaluation.getSessionId());
        
        // 각 영역별 점수 계산
        int repaymentScore = calculateRepaymentHistoryScore(evaluation);
        int debtLevelScore = calculateDebtLevelScore(evaluation);
        int creditPeriodScore = calculateCreditPeriodScore(evaluation);
        int creditPatternScore = calculateCreditPatternScore(evaluation);
        int nonFinancialScore = calculateNonFinancialScore(evaluation);
        
        // 총점 계산
        int totalScore = repaymentScore + debtLevelScore + creditPeriodScore + 
                        creditPatternScore + nonFinancialScore;
        
        // 경계값 적용
        totalScore = Math.max(MIN_TOTAL_SCORE, Math.min(MAX_TOTAL_SCORE, totalScore));
        
        log.info("신용점수 계산 완료: userId={}, totalScore={}, repayment={}, debt={}, period={}, pattern={}, nonFinancial={}", 
                evaluation.getSessionId(), totalScore, repaymentScore, debtLevelScore, 
                creditPeriodScore, creditPatternScore, nonFinancialScore);
        
        return CreditEvaluationResult.builder()
                .sessionId(evaluation.getSessionId())
                .totalScore(totalScore)
                .repaymentHistoryScore(repaymentScore)
                .debtLevelScore(debtLevelScore)
                .creditPeriodScore(creditPeriodScore)
                .creditPatternScore(creditPatternScore)
                .nonFinancialScore(nonFinancialScore)
                .scoreCalculatedDttm(LocalDateTime.now())
                .build();
    }
    
    /**
     * ① 상환이력 점수 계산 (최대 284점)
     */
    private int calculateRepaymentHistoryScore(CreditEvaluation evaluation) {
        int baseScore = MAX_REPAYMENT_SCORE;
        int deduction = 0;
        int maxDeduction = (int) (MAX_REPAYMENT_SCORE * 0.7); // 70% 한도
        
        // current_overdue_amount 감점
        BigDecimal overdueAmount = evaluation.getCurrentOverdueAmount();
        if (overdueAmount != null) {
            if (overdueAmount.compareTo(new BigDecimal("1000000")) >= 0) {
                deduction += 150; // 100만원 이상
            } else if (overdueAmount.compareTo(new BigDecimal("500000")) >= 0) {
                deduction += 100; // 50만원 이상
            } else if (overdueAmount.compareTo(new BigDecimal("100000")) >= 0) {
                deduction += 60;  // 10만원 이상
            } else if (overdueAmount.compareTo(BigDecimal.ZERO) > 0) {
                deduction += 30;  // 1원 이상
            }
        }
        
        // max_overdue_days 감점
        Integer maxOverdueDays = evaluation.getMaxOverdueDays();
        if (maxOverdueDays != null) {
            if (maxOverdueDays >= 90) {
                deduction += 120; // 90일 이상
            } else if (maxOverdueDays >= 30) {
                deduction += 80;  // 30-89일
            } else if (maxOverdueDays >= 5) {
                deduction += 40;  // 5-29일
            }
        }
        
        // recent_12m_overdue_count 감점
        Integer recentOverdueCount = evaluation.getRecent12mOverdueCount();
        if (recentOverdueCount != null) {
            deduction += recentOverdueCount * 15; // 1회당 15점
        }
        
        // loan_default_history 감점 (1: 부도이력 있음, 0: 없음)
        Integer loanDefaultHistory = evaluation.getLoanDefaultHistory();
        if (loanDefaultHistory != null && loanDefaultHistory == 1) {
            deduction += 100; // 부도이력 있음
        }
        
        // 감점 한도 적용
        deduction = Math.min(deduction, maxDeduction);
        
        return Math.max(0, baseScore - deduction);
    }
    
    /**
     * ② 부채수준 점수 계산 (최대 318점)
     */
    private int calculateDebtLevelScore(CreditEvaluation evaluation) {
        int baseScore = MAX_DEBT_LEVEL_SCORE;
        int deduction = 0;
        int bonus = 0;
        int maxDeduction = (int) (MAX_DEBT_LEVEL_SCORE * 0.7); // 70% 한도
        
        // debt_to_income_ratio 감점/가점
        BigDecimal dtiRatio = evaluation.getDebtToIncomeRatio();
        if (dtiRatio != null) {
            if (dtiRatio.compareTo(new BigDecimal("100")) >= 0) {
                deduction += 150; // DTI 100% 이상
            } else if (dtiRatio.compareTo(new BigDecimal("70")) >= 0) {
                deduction += 100; // DTI 70% 이상
            } else if (dtiRatio.compareTo(new BigDecimal("50")) >= 0) {
                deduction += 60;  // DTI 50% 이상
            } else if (dtiRatio.compareTo(new BigDecimal("30")) >= 0) {
                deduction += 20;  // DTI 30% 이상
            } else if (dtiRatio.compareTo(new BigDecimal("30")) < 0) {
                bonus += 20; // DTI 30% 미만 가점
            }
            
            if (dtiRatio.compareTo(new BigDecimal("50")) < 0) {
                bonus += 10; // DTI 50% 미만 추가 가점
            }
        }
        
        // credit_card_utilization_rate 감점
        BigDecimal cardUtilizationRate = evaluation.getCreditCardUtilizationRate();
        if (cardUtilizationRate != null) {
            if (cardUtilizationRate.compareTo(new BigDecimal("90")) >= 0) {
                deduction += 80; // 90% 이상
            } else if (cardUtilizationRate.compareTo(new BigDecimal("70")) >= 0) {
                deduction += 60; // 70% 이상
            } else if (cardUtilizationRate.compareTo(new BigDecimal("50")) >= 0) {
                deduction += 40; // 50% 이상
            } else if (cardUtilizationRate.compareTo(new BigDecimal("30")) >= 0) {
                deduction += 20; // 30% 이상
            }
        }
        
        // secured_vs_unsecured_ratio 감점
        BigDecimal securedRatio = evaluation.getSecuredVsUnsecuredRatio();
        if (securedRatio != null && securedRatio.compareTo(new BigDecimal("20")) < 0) {
            deduction += 50; // 담보대출 비중 20% 미만
        }
        
        // 감점 한도 적용
        deduction = Math.min(deduction, maxDeduction);
        
        return Math.max(0, baseScore - deduction + bonus);
    }
    
    /**
     * ③ 신용거래기간 점수 계산 (최대 123점)
     */
    private int calculateCreditPeriodScore(CreditEvaluation evaluation) {
        int score = 0;
        int deduction = 0;
        
        // credit_history_months 기본 점수
        Integer creditHistoryMonths = evaluation.getCreditHistoryMonths();
        if (creditHistoryMonths != null) {
            if (creditHistoryMonths >= 84) {        // 7년 이상
                score = 123;
            } else if (creditHistoryMonths >= 60) { // 5년 이상
                score = 105;
            } else if (creditHistoryMonths >= 36) { // 3년 이상
                score = 95;
            } else if (creditHistoryMonths >= 24) { // 2년 이상
                score = 75;
            } else if (creditHistoryMonths >= 12) { // 1년 이상
                score = 65;
            } else {                                // 1년 미만
                score = 35;
            }
        }
        
        // oldest_credit_account_months 보너스
        Integer oldestAccountMonths = evaluation.getOldestCreditAccountMonths();
        if (oldestAccountMonths != null) {
            if (oldestAccountMonths >= 120) {       // 10년 이상
                score += 15;
            } else if (oldestAccountMonths >= 84) { // 7년 이상
                score += 10;
            } else if (oldestAccountMonths >= 60) { // 5년 이상
                score += 5;
            }
        }
        
        // new_credit_inquiries_6m 감점
        Integer newInquiries = evaluation.getNewCreditInquiries6m();
        if (newInquiries != null) {
            if (newInquiries >= 5) {
                deduction = 25; // 5회 이상
            } else if (newInquiries >= 3) {
                deduction = 15; // 3-4회
            } else if (newInquiries == 2) {
                deduction = 5;  // 2회
            }
        }
        
        return Math.max(0, score - deduction);
    }
    
    /**
     * ④ 신용형태 점수 계산 (최대 275점)
     */
    private int calculateCreditPatternScore(CreditEvaluation evaluation) {
        int baseScore = 200; // 기본 점수
        int deduction = 0;
        int bonus = 0;
        int maxDeduction = (int) (MAX_CREDIT_PATTERN_SCORE * 0.7); // 192점 한도
        
        // active_credit_card_count 감점
        Integer activeCardCount = evaluation.getActiveCreditCardCount();
        if (activeCardCount != null) {
            if (activeCardCount >= 10) {
                deduction += 40; // 10장 이상
            } else if (activeCardCount >= 7) {
                deduction += 25; // 7-9장
            } else if (activeCardCount >= 5) {
                deduction += 10; // 5-6장
            }
        }
        
        // loan_type_diversity 감점/가점
        Integer loanTypeDiversity = evaluation.getLoanTypeDiversity();
        if (loanTypeDiversity != null) {
            if (loanTypeDiversity >= 5) {
                deduction += 30; // 5종류 이상
            } else if (loanTypeDiversity >= 3) {
                deduction += 15; // 3-4종류
            } else if (loanTypeDiversity == 2) {
                bonus += 10; // 2종류 (적절한 다양성)
            }
        }
        
        // financial_institution_count 감점
        Integer institutionCount = evaluation.getFinancialInstitutionCount();
        if (institutionCount != null) {
            if (institutionCount >= 7) {
                deduction += 50; // 7개 이상
            } else if (institutionCount >= 5) {
                deduction += 30; // 5-6개
            } else if (institutionCount >= 3) {
                deduction += 10; // 3-4개
            }
        }
        
        // total_credit_limit 가점 (신용한도가 높으면 가점)
        BigDecimal totalCreditLimit = evaluation.getTotalCreditLimit();
        if (totalCreditLimit != null) {
            if (totalCreditLimit.compareTo(new BigDecimal("100000000")) >= 0) { // 1억 이상
                bonus += 50;
            } else if (totalCreditLimit.compareTo(new BigDecimal("50000000")) >= 0) { // 5천만원 이상
                bonus += 30;
            } else if (totalCreditLimit.compareTo(new BigDecimal("20000000")) >= 0) { // 2천만원 이상
                bonus += 15;
            }
        }
        
        // 감점 한도 적용
        deduction = Math.min(deduction, maxDeduction);
        
        return Math.max(0, baseScore - deduction + bonus);
    }
    
    /**
     * ⑤ 비금융 점수 계산 (현재는 0점, 추후 확장 가능)
     */
    private int calculateNonFinancialScore(CreditEvaluation evaluation) {
        // alternative_credit_score가 있다면 활용
        Integer alternativeScore = evaluation.getAlternativeCreditScore();
        if (alternativeScore != null) {
            // 대안 신용점수를 0-100점 범위로 정규화하여 반환
            return Math.max(0, Math.min(100, alternativeScore));
        }
        
        return 0; // 기본값
    }
}