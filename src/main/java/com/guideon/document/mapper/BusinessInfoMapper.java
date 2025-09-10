package com.guideon.document.mapper;

import com.guideon.document.domain.BusinessInfoVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface BusinessInfoMapper {

    /**
     * 사용자 사업체 정보 저장
     */
    void insert(BusinessInfoVO businessInfoVO);

    /**
     * 사업체 정보 ID로 조회
     */
    BusinessInfoVO selectByBusinessId(Long businessId);

    /**
     * 회원 ID로 조회
     */
    BusinessInfoVO selectByMemberId(Long memberId);

    /**
     * 설문 완료시각 업데이트
     */
    int updateSurveyCompleted(BusinessInfoVO businessInfoVO);

    /**
     * 설문 초기화
     */
    int resetSurveyCompleted(Long memberId);
}
