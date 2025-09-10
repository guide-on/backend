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

}
