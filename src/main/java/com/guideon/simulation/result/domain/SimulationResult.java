package com.guideon.simulation.result.domain;

import java.time.LocalDateTime;

import com.guideon.simulation.result.enums.CurrentStep;
import com.guideon.simulation.result.enums.DocSessionStatus;
import com.guideon.simulation.result.enums.OverallStatus;

import lombok.Data;

@Data
public class SimulationResult {
    private Long id;
    private Long memberId;
    private String fundName;

    private CurrentStep currentStep;
    private OverallStatus overallStatus;
    private LocalDateTime startedAt;
    private LocalDateTime updatedAt;

    // docs
    private DocSessionStatus docSessionStatus;
    private Long businessId;

    // credit
    private Integer totalCreditScore;
    private Integer hybridCreditScore;
    private Integer traditionalCreditScore;
    private LocalDateTime creditLastUpdated;

    // plan
    private Double planTotalScore;

    // generated(read only)
    private Double docScorePct;
    private Double creditScorePct;
    private Double planScorePct;
    private Double totalProbabilityPct;
}