package com.guideon.document.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LoanSessionVO {
    private Long id;                    // id
    private Long businessId;            // 사업자 정보 아이디
    private Long policyId;              // 정책자금 아이디
    private String sessionStatus;       // 세션 상태
    private Integer requiredDocuments;  // 필요 서류 수
    private Integer submittedDocuments; // 제출 서류 수
    private Integer validatedDocuments; // 검증 완료 서류 수
    private BigDecimal progressPercentage; // 진행률
    private Date createdAt;             // 생성시각
    private Date updatedAt;             // 수정시각
}
