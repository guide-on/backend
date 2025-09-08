package com.guideon.document.dto;

import com.guideon.document.domain.BusinessInfoVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 설문으로 받은 데이터를 저장하기 위한 DTO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessInfoDTO {

    private Long memberId; // 회원 아이디
    private String loanPurpose; // 대출목적
    private String industryCode; // 업종코드
    private Integer businessPeriod; // 업력
    private Long revenue; // 연 매출
    private Integer employees; // 상시근로자 수
    private String placeType; // 사업장 형태

    public BusinessInfoVO toVO(){
        return BusinessInfoVO.builder()
                .memberId(memberId)
                .loanPurpose(loanPurpose)
                .industryCode(industryCode)
                .businessPeriod(businessPeriod)
                .revenue(revenue)
                .employees(employees)
                .placeType(placeType)
                .build();
    }
}
