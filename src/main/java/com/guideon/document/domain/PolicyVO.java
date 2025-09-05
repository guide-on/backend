package com.guideon.document.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 정책 자금 VO
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyVO {

    private Long policyId;           // id
    private String policyName;       // 정책자금명
    private String policyType;       // 자금유형 (직접/대리)
    private String loanPurpose;      // 대출목적 (운전/시설)
    private Integer minBusinessPeriod; // 최소 업력
    private Integer maxBusinessPeriod; // 최대 업력
    private Long minRevenue;         // 최소 매출
    private Long maxRevenue;         // 최대 매출
    private String requiredDocuments; // 필수 서류 (json)
    private String specialConditions; // 조건부 서류 (json)
    private Long loanLimit;          // 대출 한도(원)
    private Integer termYears;       // 대출기간(년)
    private Integer graceYears;      // 거치기간(년)
    private String quarter;          // 분기
    private BigDecimal baseRate;     // 기준금리
    private BigDecimal spreadPp;     // 가산금리
    private String rateType;         // 금리 유형(고정/변동)
    private BigDecimal maxDiscount;  // 최대우대금리
    private String discountRules;    // 우대금리 조건
    private Boolean isActive;        // 활성화 여부
    private Date createdAt;          // 생성시각
    private Date updatedAt;          // 수정시각
}