package com.guideon.hybridEvaluation.dto;

import lombok.Data;

@Data
public class StoreSummaryListRequest {
    private Long sessionId;
    private Long ownerId;
    private String businessRegistrationNo;
    private String summaryYearMonth;
    private Integer page = 1;
    private Integer limit = 20;
}