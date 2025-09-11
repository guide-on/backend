package com.guideon.simulation.result.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class SimulationResultDetailDto {
    private Long id;
    private Long memberId;
    private String fundName;

    private String currentStep;         // enum name
    private String overallStatus;       // enum name
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;

    private String docSessionStatus;    // enum name

    private Integer totalCreditScore;
    private Integer hybridCreditScore;
    private Integer traditionalCreditScore;
    private LocalDateTime creditLastUpdated;

    private Double planTotalScore;

    private Double docScorePct;
    private Double creditScorePct;
    private Double planScorePct;
    private Double totalProbabilityPct;
}
