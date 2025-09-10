package com.guideon.document.dto;

import com.guideon.document.domain.BusinessInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 설문으로 받은 데이터를 저장하기 위한 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessInfoDTO {

    private Long businessId;    // 비즈니스 아이디
    private Long memberId; // 회원 아이디
    private String loanPurpose; // 대출목적
    private String industryCode; // 업종코드
    private Integer businessPeriod; // 업력
    private Long revenue; // 연 매출
    private Integer employees; // 상시근로자 수
    private String placeType; // 사업장 형태
    private Date surveyCompletedAt; // 추가


    public BusinessInfoVO toVO(){
        return BusinessInfoVO.builder()
                .businessId(businessId)
                .memberId(memberId)
                .loanPurpose(loanPurpose)
                .industryCode(industryCode)
                .businessPeriod(businessPeriod)
                .revenue(revenue)
                .employees(employees)
                .placeType(placeType)
                .surveyCompletedAt(surveyCompletedAt)
                .build();
    }

    public static BusinessInfoDTO fromVO(BusinessInfoVO vo) {
        if (vo == null) return null;

        return BusinessInfoDTO.builder()
                .businessId(vo.getBusinessId())
                .memberId(vo.getMemberId())
                .loanPurpose(vo.getLoanPurpose())
                .industryCode(vo.getIndustryCode())
                .businessPeriod(vo.getBusinessPeriod())
                .revenue(vo.getRevenue())
                .employees(vo.getEmployees())
                .placeType(vo.getPlaceType())
                .surveyCompletedAt(vo.getSurveyCompletedAt())
                .build();
    }
}
