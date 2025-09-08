package com.guideon.hybridEvaluation.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreditEvaluationResultResponse {
    
    private Long userId;                        // 사업체 고유 식별자
    private Integer totalScore;                 // 우리 서비스 최종 신용점수
    private Integer repaymentHistoryScore;      // 상환이력 항목 점수
    private Integer debtLevelScore;             // 부채수준 항목 점수
    private Integer creditPeriodScore;          // 신용거래기간 항목 점수
    private Integer creditPatternScore;         // 신용형태 항목 점수
    private Integer nonFinancialScore;          // 비금융 항목 점수
    private LocalDateTime scoreCalculatedDttm;  // 점수가 마지막으로 계산된 시점
}