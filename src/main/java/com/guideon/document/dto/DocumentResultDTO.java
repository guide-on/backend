package com.guideon.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResultDTO {

    private String policyName;  // 정책자금명 - fund_name
    private String sessionStatus; //  서류 상태 - doc_session_status
    private BigDecimal progressPercentage; // 서류 진행률 - doc_score_pct
    private String step; // 현재 단계 - current_step "DOCS" 로 고정
}
