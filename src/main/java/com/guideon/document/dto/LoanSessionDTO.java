package com.guideon.document.dto;

import com.guideon.document.domain.LoanSessionVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanSessionDTO {

    private Long businessId;
    private Long policyId;
    private String sessionStatus;
    private Integer requiredDocuments;
    private Integer submittedDocuments;
    private Integer validatedDocuments;
    private BigDecimal progressPercentage;

    /**
     * DTO -> VO 변환
     */
    public LoanSessionVO toVO() {
        return LoanSessionVO.builder()
                .businessId(this.businessId)
                .policyId(this.policyId)
                .sessionStatus(this.sessionStatus)
                .requiredDocuments(this.requiredDocuments)
                .submittedDocuments(this.submittedDocuments)
                .validatedDocuments(this.validatedDocuments)
                .progressPercentage(this.progressPercentage)
                .build();
    }

    /**
     * 기본 세션 생성용 정적 메서드
     */
    public static LoanSessionDTO createDefault(Long businessId, Long policyId) {
        return LoanSessionDTO.builder()
                .businessId(businessId)
                .policyId(policyId)
                .sessionStatus("IN_PROGRESS")
                .requiredDocuments(0)
                .submittedDocuments(0)
                .validatedDocuments(0)
                .progressPercentage(BigDecimal.ZERO)
                .build();
    }
}