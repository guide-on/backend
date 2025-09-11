package com.guideon.plan.dto;

import com.guideon.plan.domain.SectionResultVO;
import lombok.*;

import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionDetailDTO {
    private Long sectionId;
    private String label;
    private double score;      // 득점 (가중)
    private double weight;     // 가중치
    private String comment;
    private List<String> points;
    private List<String> mappings;
    private List<String> suggestions;

    public static SectionDetailDTO from(SectionResultVO vo) {
        if (vo == null) return null;
        return SectionDetailDTO.builder()
                .sectionId(vo.getSectionId())
                .label(vo.getLabel())
                .weight(vo.getWeight())
                .score(vo.getScore())
                .comment(vo.getComment())
                .suggestions(nonNull(vo.getSuggestions()))
                .points(nonNull(vo.getPoints()))
                .mappings(nonNull(vo.getMappings()))
                .build();
    }

    private static <T> List<T> nonNull(List<T> v) { return v == null ? Collections.emptyList() : v; }
}
