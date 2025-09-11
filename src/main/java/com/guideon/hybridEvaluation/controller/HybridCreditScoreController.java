package com.guideon.hybridEvaluation.controller;

import com.guideon.hybridEvaluation.dto.HybridCreditScoreResponse;
import com.guideon.hybridEvaluation.service.HybridCreditScoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/hybrid-credit-score")
@RequiredArgsConstructor
@Slf4j
public class HybridCreditScoreController {
    
    private final HybridCreditScoreService hybridCreditScoreService;
    
    /**
     * 하이브리드 신용점수 조회
     */
    @GetMapping("/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHybridCreditScore(@PathVariable Long sessionId) {
        try {
            log.info("하이브리드 신용점수 조회 요청 - sessionId: {}", sessionId);
            
            HybridCreditScoreResponse response = hybridCreditScoreService.getHybridCreditScore(sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "하이브리드 신용점수 조회 성공");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("하이브리드 신용점수 조회 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "하이브리드 신용점수 조회 실패: " + e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 하이브리드 신용점수 계산/생성
     */
    @PostMapping("/{sessionId}/calculate")
    public ResponseEntity<Map<String, Object>> calculateHybridCreditScore(@PathVariable Long sessionId) {
        try {
            log.info("하이브리드 신용점수 계산 요청 - sessionId: {}", sessionId);
            
            HybridCreditScoreResponse response = hybridCreditScoreService.calculateHybridCreditScore(sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "하이브리드 신용점수 계산 성공");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("하이브리드 신용점수 계산 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "하이브리드 신용점수 계산 실패: " + e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * 하이브리드 신용점수 결과 조회
     */
    @GetMapping("/result/{sessionId}")
    public ResponseEntity<Map<String, Object>> getHybridCreditScoreResult(@PathVariable Long sessionId) {
        try {
            log.info("하이브리드 신용점수 결과 조회 요청 - sessionId: {}", sessionId);
            
            HybridCreditScoreResponse response = hybridCreditScoreService.getHybridCreditScoreResult(sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "하이브리드 신용점수 결과 조회 성공");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("하이브리드 신용점수 결과 조회 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "하이브리드 신용점수 결과 조회 실패: " + e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Traditional Credit Score 업데이트
     * credit_evaluation_result의 total_score를 member_credit의 traditional_credit_score로 업데이트
     */
    @PutMapping("/{sessionId}/update-traditional-score")
    public ResponseEntity<Map<String, Object>> updateTraditionalCreditScore(@PathVariable Long sessionId) {
        try {
            log.info("Traditional Credit Score 업데이트 요청 - sessionId: {}", sessionId);
            
            HybridCreditScoreResponse response = hybridCreditScoreService.updateTraditionalCreditScore(sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Traditional Credit Score 업데이트 성공");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Traditional Credit Score 업데이트 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Traditional Credit Score 업데이트 실패: " + e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Total Credit Score 계산 및 업데이트
     * hybrid_credit_score와 traditional_credit_score를 3:7 비율로 계산하여 total_credit_score 업데이트
     */
    @PutMapping("/{sessionId}/calculate-total-score")
    public ResponseEntity<Map<String, Object>> calculateAndUpdateTotalCreditScore(@PathVariable Long sessionId) {
        try {
            log.info("Total Credit Score 계산 및 업데이트 요청 - sessionId: {}", sessionId);
            
            HybridCreditScoreResponse response = hybridCreditScoreService.calculateAndUpdateTotalCreditScore(sessionId);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("message", "Total Credit Score 계산 및 업데이트 성공");
            result.put("data", response);
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            log.error("Total Credit Score 계산 및 업데이트 실패 - sessionId: {}, error: {}", sessionId, e.getMessage());
            
            Map<String, Object> error = new HashMap<>();
            error.put("success", false);
            error.put("message", "Total Credit Score 계산 및 업데이트 실패: " + e.getMessage());
            error.put("data", null);
            
            return ResponseEntity.badRequest().body(error);
        }
    }
}