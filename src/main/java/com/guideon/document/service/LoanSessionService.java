package com.guideon.document.service;

import com.guideon.document.dto.LoanSessionDTO;
import com.guideon.document.dto.SessionRequest;

import java.util.List;
import java.util.Map;

public interface LoanSessionService {

    /**
     * 대출 세션 생성
     * @param request businessId, policyId 포함
     * @return 생성/재개된 세션 정보
     */
    Map<String, Object> createLoanSession(SessionRequest request);

    /**
     * 특정 비즈니스의 모든 자금 시뮬레이션 조회
     * @param businessId 비즈니스 ID
     * @return 시뮬레이션 목록
     */
    List<LoanSessionDTO> getAllSimulationsByBusinessId(Long businessId);

    /**
     * 세션 상세 정보 조회
     * @param sessionId 세션 ID
     * @return 세션 상세 정보
     */
    LoanSessionDTO getSessionInfo(Long sessionId);
}
