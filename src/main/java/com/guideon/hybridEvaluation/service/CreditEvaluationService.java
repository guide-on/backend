package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.dto.*;
import com.guideon.common.dto.CommonResponseDTO;

import java.sql.Timestamp;
import java.util.List;

public interface CreditEvaluationService {
    
    /**
     * 신용평가 데이터 생성
     */
    CommonResponseDTO<CreditEvaluationResponse> createCreditEvaluation(CreditEvaluationCreateRequest request);
    
    /**
     * 신용평가 데이터 목록 조회 (페이징)
     */
    CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationList(CreditEvaluationListRequest request);
    
    /**
     * 하이브리드 평가 데이터 초기화 (store_summary, credit_evaluation 테이블에 기본값 생성)
     */
    CommonResponseDTO<String> initializeHybridEvaluation(Long sessionId);

    /**
     * 신용평가 관련 데이터 업데이트
     */
    CommonResponseDTO<CreditEvaluationResponse> updateCreditData(Long sessionId);
}
