package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.dto.BusinessInfoDTO;
import com.guideon.document.mapper.BusinessInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final BusinessInfoMapper businessInfoMapper;

    /**
     * 사용자 설문 응답 user_business_info에 저장
     */
    @Override
    public Long saveUserSurvey(BusinessInfoDTO businessInfoDTO) {

        // 1. 기존 설문이 있는지 확인
        BusinessInfoVO existing = businessInfoMapper.selectByMemberId(businessInfoDTO.getMemberId());

        if (existing != null && existing.getSurveyCompletedAt() != null) {
            // 이미 완료된 설문이 있으면 기존 business_id 반환
            return existing.getBusinessId();
        }

        // 2. 새로운 설문 저장 또는 기존 설문 업데이트
        BusinessInfoVO businessInfoVO = businessInfoDTO.toVO();
        businessInfoVO.setSurveyCompletedAt(new Date()); // 완료 시각 설정

        if (existing != null) {
            // 기존 레코드 업데이트 (설문 재완료)
            businessInfoVO.setBusinessId(existing.getBusinessId());
            businessInfoMapper.updateSurveyCompleted(businessInfoVO);
        } else {
            // 새 레코드 생성
            businessInfoMapper.insert(businessInfoVO);
        }

        return businessInfoVO.getBusinessId();
    }

    /**
     * 회원별 사업자 정보 조회 (설문 상태 확인)
     */
    @Override
    public BusinessInfoDTO getBusinessInfoByMemberId(Long memberId) {
        BusinessInfoVO businessInfoVO = businessInfoMapper.selectByMemberId(memberId);
        return BusinessInfoDTO.fromVO(businessInfoVO);
    }

    /**
     * 설문 초기화 - 비즈니스 아이디로 저장된 설문 정보 삭제
     */
    @Override
    @Transactional
    public void resetSurveyByBusinessId(Long businessId) {

        businessInfoMapper.deleteByBusinessId(businessId);

    }
}
