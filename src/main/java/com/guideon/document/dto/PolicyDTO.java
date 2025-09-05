package com.guideon.document.dto;

import com.guideon.document.domain.PolicyVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PolicyDTO {

    private Long policyId;           // id
    private String policyName;       // 정책자금명
    private String policyType;       // 자금유형 (직접/대리)
    private String loanPurpose;      // 대출목적 (운전/시설)
    private Long loanLimit;          // 대출 한도(원)
    private Integer termYears;       // 대출기간(년)
    private Integer graceYears;      // 거치기간(년)
    private String quarter;          // 분기
    private BigDecimal baseRate;     // 기준금리
    private BigDecimal spreadPp;     // 가산금리
    private String rateType;         // 금리 유형(고정/변동)
    private BigDecimal maxDiscount;  // 최대우대금리

    /**
     * PolicyVO -> PolicyDTO 변환
     */
    public static PolicyDTO fromVO(PolicyVO vo) {
        return PolicyDTO.builder()
                .policyId(vo.getPolicyId())
                .policyName(vo.getPolicyName())
                .policyType(vo.getPolicyType())
                .loanPurpose(vo.getLoanPurpose())
                .loanLimit(vo.getLoanLimit())
                .termYears(vo.getTermYears())
                .graceYears(vo.getGraceYears())
                .quarter(vo.getQuarter())
                .baseRate(vo.getBaseRate())
                .spreadPp(vo.getSpreadPp())
                .rateType(vo.getRateType())
                .maxDiscount(vo.getMaxDiscount())
                .build();
    }
}
