package com.guideon.document.domain;

import lombok.*;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BusinessInfoVO {

    private Long businessId; // 사업체 아이디
    private Long memberId; // 회원 아이디
    private String loanPurpose; // 대출목적
    private String industryCode; // 업종코드
    private Integer businessPeriod; // 업력
    private Long revenue; // 연 매출
    private Integer employees; // 상시근로자 수
    private String placeType; // 사업장 형태
    private Date createdAt; // 생성일
}
