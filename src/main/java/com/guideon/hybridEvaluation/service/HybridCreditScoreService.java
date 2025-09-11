package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.dto.HybridCreditScoreResponse;

public interface HybridCreditScoreService {
    
    /**
     * 하이브리드 신용점수 조회
     */
    HybridCreditScoreResponse getHybridCreditScore(Long sessionId);
    
    /**
     * 하이브리드 신용점수 계산/생성
     */
    HybridCreditScoreResponse calculateHybridCreditScore(Long sessionId);
    
    /**
     * 하이브리드 신용점수 결과 조회
     */
    HybridCreditScoreResponse getHybridCreditScoreResult(Long sessionId);
    
    /**
     * traditional_credit_score 업데이트
     * credit_evaluation_result의 total_score를 member_credit의 traditional_credit_score로 업데이트
     */
    HybridCreditScoreResponse updateTraditionalCreditScore(Long sessionId);
    
    /**
     * total_credit_score 계산 및 업데이트
     * hybrid_credit_score와 traditional_credit_score를 3:7 비율로 계산하여 total_credit_score 업데이트
     */
    HybridCreditScoreResponse calculateAndUpdateTotalCreditScore(Long sessionId);
}