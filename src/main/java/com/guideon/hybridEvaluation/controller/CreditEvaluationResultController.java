package com.guideon.hybridEvaluation.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;
import com.guideon.hybridEvaluation.service.CreditEvaluationResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/credit-evaluation-result")
@RequiredArgsConstructor
@Api(tags = "신용평가 결과 관리")
public class CreditEvaluationResultController {
    
    private final CreditEvaluationResultService creditEvaluationResultService;
    
    @GetMapping("/{sessionId}")
    @ApiOperation(value = "특정 사용자 신용평가 결과 조회", notes = "특정 사용자의 신용평가 결과를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResultResponse>> getCreditEvaluationResult(
            @ApiParam(value = "사용자 ID (session_id)", required = true, example = "1001")
            @PathVariable Long sessionId) {

        log.info("신용평가 결과 조회 요청: sessionId={}", sessionId);

        CommonResponseDTO<CreditEvaluationResultResponse> response =
            creditEvaluationResultService.getCreditEvaluationResult(sessionId);

        return ResponseEntity.ok(response);
    }
}