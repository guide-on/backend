package com.guideon.plan.ai.domain;

import com.fasterxml.jackson.annotation.JsonClassDescription;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

import java.util.List;

@Data
@JsonClassDescription("섹션별 평가 결과")
public class AiSectionRow {
    @JsonPropertyDescription("plan_eval_section.section_id (DB의 PK)")
    public long sectionId;

    @JsonPropertyDescription("해당 섹션 가중 득점 (0 ~ 해당 섹션 weight)")
    public double scorePoints;

    @JsonPropertyDescription("한두 문장 코멘트")
    public String comment;

    @JsonPropertyDescription("개선 제안 (최대 3개)")
    public List<String> suggestions;
}
