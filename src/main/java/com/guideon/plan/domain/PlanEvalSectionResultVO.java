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
public class PlanEvalSectionResultVO {
    private Long sectionResultId;
    private Long reportId;
    private Long sectionId;
    private Double scorePoints;
    private String comment;
    private List<String> suggestions; // JSON 문자열
}
