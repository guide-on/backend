package com.guideon.hybridEvaluation.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultCreateRequest;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultUpdateRequest;
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
    
    @PostMapping
    @ApiOperation(value = "신용평가 결과 생성", notes = "새로운 신용평가 결과를 생성합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResultResponse>> createCreditEvaluationResult(
            @RequestBody CreditEvaluationResultCreateRequest request) {
        
        log.info("신용평가 결과 생성 요청: userId={}", request.getUserId());
        
        CommonResponseDTO<CreditEvaluationResultResponse> response = 
            creditEvaluationResultService.createCreditEvaluationResult(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    @ApiOperation(value = "신용평가 결과 수정", notes = "기존 신용평가 결과를 수정합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResultResponse>> updateCreditEvaluationResult(
            @RequestBody CreditEvaluationResultUpdateRequest request) {
        
        log.info("신용평가 결과 수정 요청: userId={}", request.getUserId());
        
        CommonResponseDTO<CreditEvaluationResultResponse> response = 
            creditEvaluationResultService.updateCreditEvaluationResult(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}")
    @ApiOperation(value = "특정 사용자 신용평가 결과 조회", notes = "특정 사용자의 신용평가 결과를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResultResponse>> getCreditEvaluationResult(
            @ApiParam(value = "사용자 ID", required = true, example = "1001") 
            @PathVariable Long userId) {
        
        log.info("신용평가 결과 조회 요청: userId={}", userId);
        
        CommonResponseDTO<CreditEvaluationResultResponse> response = 
            creditEvaluationResultService.getCreditEvaluationResult(userId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @ApiOperation(value = "신용평가 결과 목록 조회", notes = "모든 신용평가 결과 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResultResponse>>> getCreditEvaluationResultList() {
        
        log.info("신용평가 결과 목록 조회 요청");
        
        CommonResponseDTO<List<CreditEvaluationResultResponse>> response = 
            creditEvaluationResultService.getCreditEvaluationResultList();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/score-range")
    @ApiOperation(value = "점수 범위별 신용평가 결과 조회", notes = "지정된 점수 범위의 신용평가 결과를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResultResponse>>> getCreditEvaluationResultByScoreRange(
            @ApiParam(value = "최소 점수", example = "600") 
            @RequestParam(required = false) Integer minScore,
            @ApiParam(value = "최대 점수", example = "800") 
            @RequestParam(required = false) Integer maxScore) {
        
        log.info("점수 범위별 신용평가 결과 조회 요청: minScore={}, maxScore={}", minScore, maxScore);
        
        CommonResponseDTO<List<CreditEvaluationResultResponse>> response = 
            creditEvaluationResultService.getCreditEvaluationResultByScoreRange(minScore, maxScore);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{userId}")
    @ApiOperation(value = "신용평가 결과 삭제", notes = "특정 사용자의 신용평가 결과를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteCreditEvaluationResult(
            @ApiParam(value = "사용자 ID", required = true, example = "1001") 
            @PathVariable Long userId) {
        
        log.info("신용평가 결과 삭제 요청: userId={}", userId);
        
        CommonResponseDTO<Void> response = 
            creditEvaluationResultService.deleteCreditEvaluationResult(userId);
        
        return ResponseEntity.ok(response);
    }
}