package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultCreateRequest;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultUpdateRequest;

import java.util.List;

public interface CreditEvaluationResultService {
    
    /**
     * 신용평가 결과 생성
     * @param request 생성 요청 데이터
     * @return 생성된 신용평가 결과
     */
    CommonResponseDTO<CreditEvaluationResultResponse> createCreditEvaluationResult(
            CreditEvaluationResultCreateRequest request);
    
    /**
     * 신용평가 결과 수정
     * @param request 수정 요청 데이터
     * @return 수정된 신용평가 결과
     */
    CommonResponseDTO<CreditEvaluationResultResponse> updateCreditEvaluationResult(
            CreditEvaluationResultUpdateRequest request);
    
    /**
     * 특정 사용자의 신용평가 결과 조회
     * @param memberId 사용자 ID
     * @return 신용평가 결과
     */
    CommonResponseDTO<CreditEvaluationResultResponse> getCreditEvaluationResult(Long memberId);
    
    /**
     * 모든 신용평가 결과 목록 조회
     * @return 신용평가 결과 목록
     */
    CommonResponseDTO<List<CreditEvaluationResultResponse>> getCreditEvaluationResultList();
    
    /**
     * 점수 범위별 신용평가 결과 조회
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @return 해당 점수 범위의 신용평가 결과 목록
     */
    CommonResponseDTO<List<CreditEvaluationResultResponse>> getCreditEvaluationResultByScoreRange(
            Integer minScore, Integer maxScore);
    
    /**
     * 신용평가 결과 삭제
     * @param memberId 사용자 ID
     * @return 삭제 결과
     */
    CommonResponseDTO<Void> deleteCreditEvaluationResult(Long memberId);
}