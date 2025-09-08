package com.guideon.document.service;

import com.guideon.document.dto.BusinessInfoDTO;

public interface SurveyService {

    /**
     * 사용자 설문 응답 저장
     */
    public Long saveUserSurvey(BusinessInfoDTO businessInfoDTO);
}
