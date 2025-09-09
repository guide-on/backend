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
     * 신용평가 데이터 수정
     */
    CommonResponseDTO<CreditEvaluationResponse> updateCreditEvaluation(CreditEvaluationUpdateRequest request);
    
    /**
     * 특정 사용자의 특정 평가일자 데이터 조회
     */
    CommonResponseDTO<CreditEvaluationResponse> getCreditEvaluation(String userId, Timestamp evaluationDate);
    
    /**
     * 특정 사용자의 최신 신용평가 데이터 조회
     */
    CommonResponseDTO<CreditEvaluationResponse> getLatestCreditEvaluation(String userId);
    
    /**
     * 신용평가 데이터 목록 조회 (페이징)
     */
    CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationList(CreditEvaluationListRequest request);
    
    /**
     * 특정 사용자의 신용평가 이력 조회
     */
    CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationHistory(String userId, Integer page, Integer limit);
    
    /**
     * 신용평가 데이터 삭제
     */
    CommonResponseDTO<Void> deleteCreditEvaluation(String userId, Timestamp evaluationDate);
    
    /**
     * 특정 사용자의 모든 신용평가 데이터 삭제
     */
    CommonResponseDTO<Void> deleteAllCreditEvaluationByUserId(String userId);
}
