package com.guideon.document.service;

import com.guideon.document.dto.PolicyDTO;

import java.util.List;

public interface PolicyService {

    /**
     * 소상공인 자격 판별 메서드
     * @param businessId : 사업자 정보 아이디
     * @return 소상공인이면 true, 아니면 false
     */
    boolean isSmallBusiness(Long businessId);

    /**
     * 사업체 정보 기반 적합한 정책자금 필터링 메서드
     * @param businessId : 사업자 정보 아이디
     * @return 사업자 정보 기반 신청가능한 정책자금 DTO 리스트
     */
    List<PolicyDTO> filterEligiblePolicies(Long businessId);
}
