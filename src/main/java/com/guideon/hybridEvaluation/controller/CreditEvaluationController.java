package com.guideon.hybridEvaluation.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.*;
import com.guideon.hybridEvaluation.service.CreditEvaluationService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/credit-evaluation")
@RequiredArgsConstructor
@Api(tags = "신용평가 관리")
public class CreditEvaluationController {
    
    private final CreditEvaluationService creditEvaluationService;
    
    @PostMapping
    @ApiOperation(value = "신용평가 데이터 생성", notes = "새로운 신용평가 데이터를 생성합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> createCreditEvaluation(
            @RequestBody CreditEvaluationCreateRequest request) {
        
        log.info("신용평가 데이터 생성 요청: userId={}", request.getUserId());
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.createCreditEvaluation(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    @ApiOperation(value = "신용평가 데이터 수정", notes = "기존 신용평가 데이터를 수정합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> updateCreditEvaluation(
            @RequestBody CreditEvaluationUpdateRequest request) {
        
        log.info("신용평가 데이터 수정 요청: userId={}, evaluationDate={}", 
                request.getUserId(), request.getEvaluationDate());
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.updateCreditEvaluation(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}")
    @ApiOperation(value = "특정 평가일자 신용평가 데이터 조회", notes = "특정 사용자의 특정 평가일자 신용평가 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> getCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String userId,
            @ApiParam(value = "평가일자 (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime evaluationDate) {
        
        log.info("신용평가 데이터 조회 요청: userId={}, evaluationDate={}", userId, evaluationDate);
        
        Timestamp timestamp = Timestamp.valueOf(evaluationDate);
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.getCreditEvaluation(userId, timestamp);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}/latest")
    @ApiOperation(value = "최신 신용평가 데이터 조회", notes = "특정 사용자의 최신 신용평가 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> getLatestCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String userId) {
        
        log.info("최신 신용평가 데이터 조회 요청: userId={}", userId);
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.getLatestCreditEvaluation(userId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @ApiOperation(value = "신용평가 데이터 목록 조회", notes = "조건에 따른 신용평가 데이터 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResponse>>> getCreditEvaluationList(
            @ApiParam(value = "사용자 ID (선택)") @RequestParam(required = false) String userId,
            @ApiParam(value = "시작일자 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @ApiParam(value = "종료일자 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit,
            @ApiParam(value = "정렬 기준", defaultValue = "evaluationDate") @RequestParam(defaultValue = "evaluationDate") String sortBy,
            @ApiParam(value = "정렬 순서", defaultValue = "DESC") @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("신용평가 데이터 목록 조회 요청: userId={}, page={}, limit={}", userId, page, limit);
        
        CreditEvaluationListRequest request = new CreditEvaluationListRequest();
        request.setUserId(userId);
        request.setStartDate(startDate != null ? Timestamp.valueOf(startDate) : null);
        request.setEndDate(endDate != null ? Timestamp.valueOf(endDate) : null);
        request.setPage(page);
        request.setLimit(limit);
        request.setSortBy(sortBy);
        request.setSortOrder(sortOrder);
        
        CommonResponseDTO<List<CreditEvaluationResponse>> response = 
            creditEvaluationService.getCreditEvaluationList(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{userId}/history")
    @ApiOperation(value = "사용자 신용평가 이력 조회", notes = "특정 사용자의 신용평가 이력을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResponse>>> getCreditEvaluationHistory(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String userId,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("사용자 신용평가 이력 조회 요청: userId={}, page={}, limit={}", userId, page, limit);
        
        CommonResponseDTO<List<CreditEvaluationResponse>> response = 
            creditEvaluationService.getCreditEvaluationHistory(userId, page, limit);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{userId}")
    @ApiOperation(value = "신용평가 데이터 삭제", notes = "특정 사용자의 특정 평가일자 신용평가 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String userId,
            @ApiParam(value = "평가일자 (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime evaluationDate) {
        
        log.info("신용평가 데이터 삭제 요청: userId={}, evaluationDate={}", userId, evaluationDate);
        
        Timestamp timestamp = Timestamp.valueOf(evaluationDate);
        CommonResponseDTO<Void> response = 
            creditEvaluationService.deleteCreditEvaluation(userId, timestamp);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{userId}/all")
    @ApiOperation(value = "사용자 모든 신용평가 데이터 삭제", notes = "특정 사용자의 모든 신용평가 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAllCreditEvaluationByUserId(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String userId) {
        
        log.info("사용자 모든 신용평가 데이터 삭제 요청: userId={}", userId);
        
        CommonResponseDTO<Void> response = 
            creditEvaluationService.deleteAllCreditEvaluationByUserId(userId);
        
        return ResponseEntity.ok(response);
    }
}

