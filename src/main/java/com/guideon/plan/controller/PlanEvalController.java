package com.guideon.plan.controller;

import com.guideon.plan.dto.EvaluationResultDTO;
import com.guideon.plan.parser.PdfPlanExtractor;
import com.guideon.plan.service.PlanEvalService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/plan-eval")
public class PlanEvalController {
    private final PlanEvalService planEvalService;

    @PostMapping("/session/{sessionId}/evaluate")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public PdfPlanExtractor evaluateBySession(@PathVariable Long sessionId) throws Exception {
        return planEvalService.evaluateDocument(sessionId);
    }

    @GetMapping("/{sessionId}")
    @ResponseBody
    public EvaluationResultDTO get(@PathVariable("sessionId") Long sessionId) {
        return planEvalService.getByReportId(sessionId);
    }

    @PostMapping("/session/{sessionId}/evaluate/ai")
    @ResponseBody
    public ResponseEntity<Void> evaluateWithAi(@PathVariable Long sessionId) throws Exception {
        planEvalService.evaluateSessionWithAI(sessionId);
        return ResponseEntity.noContent().build(); // 204
    }
}
