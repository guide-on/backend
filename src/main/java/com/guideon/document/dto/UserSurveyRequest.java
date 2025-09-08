package com.guideon.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 사용자 설문 조사 요청 DTO
 * : 설문에서 입력한 요청 데이터
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserSurveyRequest {

    private String loanPurpose; // 대출목적
    private Integer businessPeriod; // 업력
    private Long revenue; // 연 매출
    private Integer employees; // 상시근로자 수
    private String placeType; // 사업장 형태

}
