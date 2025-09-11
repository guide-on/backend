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
public class SectionResultVO {
    private Long sectionId;
    private String label;
    private Double weight;
    private Double score;
    private String comment;

    private List<String> points;      // JSON
    private List<String> mappings;    // JSON
    private List<String> suggestions; // JSON
}
