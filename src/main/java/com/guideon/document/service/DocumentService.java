package com.guideon.document.service;

import com.guideon.document.dto.SessionRequest;

import java.util.Map;

public interface DocumentService {

    /**
     * 대출 세션 생성
     * @param request businessId, policyId 포함
     * @return 생성된 세션 정보
     */
    Map<String, Object> createLoanSession(SessionRequest request);

    /**
     * 세션별 필요 서류 목록 조회
     */
    Map<String, Object> getRequiredDocuments(Long sessionId);
}