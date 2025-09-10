package com.guideon.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentResultDTO {

    private Long memberId;
    private String policyName;
    private String sessionStatus;
    private String progressPercentage;
}
