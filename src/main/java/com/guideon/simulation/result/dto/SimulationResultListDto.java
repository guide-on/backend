package com.guideon.simulation.result.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SimulationResultListDto {
    private Long id;
    private LocalDateTime startedAt;
    private String fundName;
    private String currentStep;         // enum name
    private String overallStatus;       // enum name
    private Double totalProbabilityPct;
    private Double docScorePct;
    private Double creditScorePct;
    private Double planScorePct;
}
