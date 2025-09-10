package com.guideon.document.service;

import com.guideon.document.dto.BusinessInfoDTO;

public interface SurveyService {

    /**
     * 사용자 설문 응답 저장 및 마스터 세션 생성
     */
    Long saveUserSurvey(BusinessInfoDTO businessInfoDTO);

    /**
     * 회원별 사업자 정보 조회 (설문 상태 확인)
     */
    BusinessInfoDTO getBusinessInfoByMemberId(Long memberId);

    /**
     * 비즈니스 ID로 설문 초기화 (CASCADE로 세션들도 함께 삭제)
     */
    void resetSurveyByBusinessId(Long businessId);
}
