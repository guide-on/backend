package com.guideon.plan.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PlanEvalReportVO {
    private Long reportId;
    private Long memberId;
    private Long documentId;
    private Long sessionId;
    private Double totalScore;
    private List<String> strengths; // JSON
    private List<String> risks;     // JSON
    private String errorCode;
    private String errorMessage;
}
