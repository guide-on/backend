package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.dto.BusinessInfoDTO;
import com.guideon.document.mapper.BusinessInfoMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SurveyServiceImpl implements SurveyService {

    private final BusinessInfoMapper businessInfoMapper;

    /**
     * 사용자 설문 응답 user_business_info에 저장
     */
    @Override
    public Long saveUserSurvey(BusinessInfoDTO businessInfoDTO) {

        // 1. DTO의 toVO() 메소드 사용하여 변환
        BusinessInfoVO businessInfoVO = businessInfoDTO.toVO();

        // 2. DB 저장
        businessInfoMapper.insert(businessInfoVO);

        // 3. 생성된 businessId 반환
        return businessInfoVO.getBusinessId();
    }
}
