package com.guideon.document.service;

import com.guideon.document.dto.DocumentResultDTO;
import com.guideon.document.dto.SessionRequest;

import java.util.Map;

public interface LoanSessionService {

    /**
     * 대출 세션 생성
     * @param request businessId, policyId 포함
     * @return 생성/재개된 세션 정보
     */
    Map<String, Object> createLoanSession(SessionRequest request);

    /**
     * 서류 결과 생성
     */
    Map<String, Object> createDocsResult(SessionRequest request);

    /**
     * 서류 업데이트 시 DocumentResult도 함께 업데이트
     */
    void updateDocumentResult(Long sessionId, DocumentResultDTO documentResult);

    /**
     * 현재 진행 단계 조회
     */
    String getCurrentStep(Long sessionId);

}
