package com.guideon.hybridEvaluation.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.*;
import com.guideon.hybridEvaluation.service.CreditEvaluationService;
import com.guideon.hybridEvaluation.service.CreditEvaluationResultService;
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
    private final CreditEvaluationResultService creditEvaluationResultService;
    
    @PostMapping
    @ApiOperation(value = "신용평가 데이터 생성", notes = "새로운 신용평가 데이터를 생성합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> createCreditEvaluation(
            @RequestBody CreditEvaluationCreateRequest request) {
        
        log.info("신용평가 데이터 생성 요청: sessionId={}", request.getSessionId());
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.createCreditEvaluation(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    @ApiOperation(value = "신용평가 데이터 수정", notes = "기존 신용평가 데이터를 수정합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> updateCreditEvaluation(
            @RequestBody CreditEvaluationUpdateRequest request) {
        
        log.info("신용평가 데이터 수정 요청: sessionId={}, evaluationDate={}", 
                request.getSessionId(), request.getEvaluationDate());
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.updateCreditEvaluation(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{sessionId}")
    @ApiOperation(value = "특정 평가일자 신용평가 데이터 조회", notes = "특정 사용자의 특정 평가일자 신용평가 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> getCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "평가일자 (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime evaluationDate) {
        
        log.info("신용평가 데이터 조회 요청: sessionId={}, evaluationDate={}", sessionId, evaluationDate);
        
        Timestamp timestamp = Timestamp.valueOf(evaluationDate);
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.getCreditEvaluation(sessionId, timestamp);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{sessionId}/latest")
    @ApiOperation(value = "최신 신용평가 데이터 조회", notes = "특정 사용자의 최신 신용평가 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<CreditEvaluationResponse>> getLatestCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId) {
        
        log.info("최신 신용평가 데이터 조회 요청: sessionId={}", sessionId);
        
        CommonResponseDTO<CreditEvaluationResponse> response = 
            creditEvaluationService.getLatestCreditEvaluation(sessionId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @ApiOperation(value = "신용평가 데이터 목록 조회", notes = "조건에 따른 신용평가 데이터 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResponse>>> getCreditEvaluationList(
            @ApiParam(value = "사용자 ID (선택)") @RequestParam(required = false) String sessionId,
            @ApiParam(value = "시작일자 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime startDate,
            @ApiParam(value = "종료일자 (yyyy-MM-dd HH:mm:ss)") @RequestParam(required = false) 
            @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime endDate,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit,
            @ApiParam(value = "정렬 기준", defaultValue = "evaluationDate") @RequestParam(defaultValue = "evaluationDate") String sortBy,
            @ApiParam(value = "정렬 순서", defaultValue = "DESC") @RequestParam(defaultValue = "DESC") String sortOrder) {
        
        log.info("신용평가 데이터 목록 조회 요청: sessionId={}, page={}, limit={}", sessionId, page, limit);
        
        CreditEvaluationListRequest request = new CreditEvaluationListRequest();
        request.setSessionId(sessionId);
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
    
    @GetMapping("/{sessionId}/history")
    @ApiOperation(value = "사용자 신용평가 이력 조회", notes = "특정 사용자의 신용평가 이력을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<CreditEvaluationResponse>>> getCreditEvaluationHistory(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("사용자 신용평가 이력 조회 요청: sessionId={}, page={}, limit={}", sessionId, page, limit);
        
        CommonResponseDTO<List<CreditEvaluationResponse>> response = 
            creditEvaluationService.getCreditEvaluationHistory(sessionId, page, limit);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{sessionId}")
    @ApiOperation(value = "신용평가 데이터 삭제", notes = "특정 사용자의 특정 평가일자 신용평가 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteCreditEvaluation(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId,
            @ApiParam(value = "평가일자 (yyyy-MM-dd HH:mm:ss)", required = true)
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime evaluationDate) {
        
        log.info("신용평가 데이터 삭제 요청: sessionId={}, evaluationDate={}", sessionId, evaluationDate);
        
        Timestamp timestamp = Timestamp.valueOf(evaluationDate);
        CommonResponseDTO<Void> response = 
            creditEvaluationService.deleteCreditEvaluation(sessionId, timestamp);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{sessionId}/all")
    @ApiOperation(value = "사용자 모든 신용평가 데이터 삭제", notes = "특정 사용자의 모든 신용평가 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAllCreditEvaluationByMemberId(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId) {
        
        log.info("사용자 모든 신용평가 데이터 삭제 요청: sessionId={}", sessionId);
        
        CommonResponseDTO<Void> response = 
            creditEvaluationService.deleteAllCreditEvaluationBySessionId(sessionId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{sessionId}/score-result")
    @ApiOperation(value = "신용평가 데이터와 계산된 점수 결과 함께 조회", 
                  notes = "특정 사용자의 최신 신용평가 데이터와 계산된 점수 결과를 함께 조회합니다.")
    public ResponseEntity<String> getCreditEvaluationWithScore(
            @ApiParam(value = "사용자 ID", required = true) @PathVariable String sessionId) {
        
        log.info("신용평가 데이터 및 점수 결과 조회 요청: sessionId={}", sessionId);
        
        try {
            // 최신 신용평가 데이터 조회
            CommonResponseDTO<CreditEvaluationResponse> evaluationResponse = 
                creditEvaluationService.getLatestCreditEvaluation(sessionId);
            
            // 점수 결과 조회
            Long sessionIdLong = Long.valueOf(sessionId);
            CommonResponseDTO<CreditEvaluationResultResponse> scoreResponse = 
                creditEvaluationResultService.getCreditEvaluationResult(sessionIdLong);
            
            // 결과 조합
            StringBuilder result = new StringBuilder();
            result.append("=== 신용평가 데이터 ===\n");
            if (evaluationResponse.isSuccess() && evaluationResponse.getData() != null) {
                CreditEvaluationResponse data = evaluationResponse.getData();
                result.append(String.format("사용자ID: %s\n", data.getSessionId()));
                result.append(String.format("평가일시: %s\n", data.getEvaluationDate()));
                result.append(String.format("현재연체금액: %s\n", data.getCurrentOverdueAmount()));
                result.append(String.format("최대연체일수: %d일\n", data.getMaxOverdueDays()));
                result.append(String.format("최근12개월연체횟수: %d회\n", data.getRecent12mOverdueCount()));
                result.append(String.format("부채소득비율: %s%%\n", data.getDebtToIncomeRatio()));
                result.append(String.format("신용카드이용률: %s%%\n", data.getCreditCardUtilizationRate()));
                result.append(String.format("신용거래기간: %d개월\n", data.getCreditHistoryMonths()));
                result.append(String.format("활성신용카드수: %d장\n", data.getActiveCreditCardCount()));
                result.append(String.format("총신용한도: %s원\n", data.getTotalCreditLimit()));
            } else {
                result.append("신용평가 데이터 없음\n");
            }
            
            result.append("\n=== 계산된 점수 결과 ===\n");
            if (scoreResponse.isSuccess() && scoreResponse.getData() != null) {
                CreditEvaluationResultResponse data = scoreResponse.getData();
                result.append(String.format("총점: %d점\n", data.getTotalScore()));
                result.append(String.format("상환이력 점수: %d점\n", data.getRepaymentHistoryScore()));
                result.append(String.format("부채수준 점수: %d점\n", data.getDebtLevelScore()));
                result.append(String.format("신용거래기간 점수: %d점\n", data.getCreditPeriodScore()));
                result.append(String.format("신용형태 점수: %d점\n", data.getCreditPatternScore()));
                result.append(String.format("비금융 점수: %d점\n", data.getNonFinancialScore()));
                result.append(String.format("점수계산일시: %s\n", data.getScoreCalculatedDttm()));
            } else {
                result.append("점수 결과 없음\n");
            }
            
            return ResponseEntity.ok(result.toString());
            
        } catch (Exception e) {
            log.error("신용평가 데이터 및 점수 결과 조회 중 오류: {}", e.getMessage(), e);
            return ResponseEntity.ok("오류 발생: " + e.getMessage());
        }
    }
}

