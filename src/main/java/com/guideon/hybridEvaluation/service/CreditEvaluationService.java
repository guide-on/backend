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
}
