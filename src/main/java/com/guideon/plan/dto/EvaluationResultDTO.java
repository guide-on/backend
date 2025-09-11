package com.guideon.plan.dto;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EvaluationResultDTO {
    private Long reportId;
    private Long documentId;
    private Double totalScore;      // 0~100
    private String grade;          // A+, A, B+, B, C, D, F
    private List<String> strengths; // 강점
    private List<String> risks;     // 리스크/개선
    private List<SectionDetailDTO> sections;
}
