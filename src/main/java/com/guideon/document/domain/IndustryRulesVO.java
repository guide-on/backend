package com.guideon.document.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class IndustryRulesVO {
    private Long id;                    // id
    private String industryCode;        // 업종코드
    private String industryName;        // 업종명
    private Boolean isLoanEligible;     // 융자조건 제외 여부
    private Integer employeeLimit;      // 상시근로자 제한
    private Boolean hasSpecialCondition; // 특별 조건
    private Long maxRevenue;            // 최대 매출액
}
