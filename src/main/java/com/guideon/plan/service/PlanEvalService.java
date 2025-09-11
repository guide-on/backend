package com.guideon.plan.service;

import com.guideon.plan.dto.EvaluationResultDTO;
import com.guideon.plan.parser.PdfPlanExtractor;

public interface PlanEvalService {
    PdfPlanExtractor evaluateDocument(Long sessionId) throws Exception;
    EvaluationResultDTO getByReportId(Long reportId);
}
