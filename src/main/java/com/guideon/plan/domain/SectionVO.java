package com.guideon.plan.domain;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionVO {
    private Long sectionId;
    private String displayLabel;
    private Double weight;
    private List<String> formMappings;   // JSON
    private List<String> pointsTemplate; // JSON
}
