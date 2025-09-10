package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;

public interface CreditEvaluationResultService {

    /**
     * 특정 사용자의 신용평가 결과 조회
     * @param sessionId 사용자 ID
     * @return 신용평가 결과
     */
    CommonResponseDTO<CreditEvaluationResultResponse> getCreditEvaluationResult(Long sessionId);
}