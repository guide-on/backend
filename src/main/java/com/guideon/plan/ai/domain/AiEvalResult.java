package com.guideon.plan.ai.domain;

import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
public class AiEvalResult {
    @JsonPropertyDescription("섹션 가중 득점의 합(0~100). 반올림 없이 원점수.")
    public double totalScore;

    @JsonPropertyDescription("문서 전반 강점 (최대 5개)")
    public List<String> strengths;

    @JsonPropertyDescription("문서 전반 리스크/개선 필요 (최대 5개)")
    public List<String> risks;

    @JsonPropertyDescription("섹션별 결과 목록")
    public List<AiSectionRow> sections;
}
