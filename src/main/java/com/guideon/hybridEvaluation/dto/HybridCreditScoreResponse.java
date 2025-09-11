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
public class HybridCreditScoreResponse {
    
    private Long sessionId;                    // 사업체 고유 식별자
    private Integer totalCreditScore;          // 최종 신용점수 (0 ~ 1000점 변환)
    private Integer hybridCreditScore;         // 하이브리드 신용점수
    private Integer traditionalCreditScore;    // 기존 신용점수
    private LocalDateTime lastUpdatedDttm;     // 최종 갱신 시점
    private Integer salesSummaryScoreScaled;   // 매출 요약 점수
    private Integer financialInfoScoreScaled;  // 재무 정보 점수
    private Integer operationalInfoScoreScaled; // 운영 정보 점수
}