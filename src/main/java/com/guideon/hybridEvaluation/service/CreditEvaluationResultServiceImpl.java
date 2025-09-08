package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultCreateRequest;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultUpdateRequest;
import com.guideon.hybridEvaluation.exception.CreditEvaluationNotFoundException;
import com.guideon.hybridEvaluation.exception.CreditEvaluationValidationException;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CreditEvaluationResultServiceImpl implements CreditEvaluationResultService {
    
    private final CreditEvaluationResultMapper creditEvaluationResultMapper;
    
    @Override
    @Transactional
    public CommonResponseDTO<CreditEvaluationResultResponse> createCreditEvaluationResult(
            CreditEvaluationResultCreateRequest request) {
        
        log.info("신용평가 결과 생성 시작: userId={}", request.getUserId());
        
        // 중복 체크
        if (creditEvaluationResultMapper.existsCreditEvaluationResult(request.getUserId())) {
            throw new CreditEvaluationValidationException(
                "이미 해당 사용자의 신용평가 결과가 존재합니다. userId: " + request.getUserId());
        }
        
        // Domain 객체 생성
        CreditEvaluationResult creditEvaluationResult = CreditEvaluationResult.builder()
                .userId(request.getUserId())
                .totalScore(request.getTotalScore())
                .repaymentHistoryScore(request.getRepaymentHistoryScore())
                .debtLevelScore(request.getDebtLevelScore())
                .creditPeriodScore(request.getCreditPeriodScore())
                .creditPatternScore(request.getCreditPatternScore())
                .nonFinancialScore(request.getNonFinancialScore())
                .scoreCalculatedDttm(LocalDateTime.now())
                .build();
        
        // 데이터베이스 저장
        int insertedRows = creditEvaluationResultMapper.insertCreditEvaluationResult(creditEvaluationResult);
        
        if (insertedRows == 0) {
            throw new CreditEvaluationValidationException("신용평가 결과 생성에 실패했습니다.");
        }
        
        // 저장된 데이터 조회
        CreditEvaluationResult savedResult = creditEvaluationResultMapper
                .selectCreditEvaluationResult(request.getUserId());
        
        CreditEvaluationResultResponse response = convertToResponse(savedResult);
        
        log.info("신용평가 결과 생성 완료: userId={}, totalScore={}", 
                request.getUserId(), response.getTotalScore());
        
        return CommonResponseDTO.success("신용평가 결과가 성공적으로 생성되었습니다.", response);
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<CreditEvaluationResultResponse> updateCreditEvaluationResult(
            CreditEvaluationResultUpdateRequest request) {
        
        log.info("신용평가 결과 수정 시작: userId={}", request.getUserId());
        
        // 존재 여부 체크
        if (!creditEvaluationResultMapper.existsCreditEvaluationResult(request.getUserId())) {
            throw new CreditEvaluationNotFoundException(
                "해당 사용자의 신용평가 결과를 찾을 수 없습니다. userId: " + request.getUserId());
        }
        
        // Domain 객체 생성
        CreditEvaluationResult creditEvaluationResult = CreditEvaluationResult.builder()
                .userId(request.getUserId())
                .totalScore(request.getTotalScore())
                .repaymentHistoryScore(request.getRepaymentHistoryScore())
                .debtLevelScore(request.getDebtLevelScore())
                .creditPeriodScore(request.getCreditPeriodScore())
                .creditPatternScore(request.getCreditPatternScore())
                .nonFinancialScore(request.getNonFinancialScore())
                .scoreCalculatedDttm(LocalDateTime.now())
                .build();
        
        // 데이터베이스 수정
        int updatedRows = creditEvaluationResultMapper.updateCreditEvaluationResult(creditEvaluationResult);
        
        if (updatedRows == 0) {
            throw new CreditEvaluationValidationException("신용평가 결과 수정에 실패했습니다.");
        }
        
        // 수정된 데이터 조회
        CreditEvaluationResult updatedResult = creditEvaluationResultMapper
                .selectCreditEvaluationResult(request.getUserId());
        
        CreditEvaluationResultResponse response = convertToResponse(updatedResult);
        
        log.info("신용평가 결과 수정 완료: userId={}, totalScore={}", 
                request.getUserId(), response.getTotalScore());
        
        return CommonResponseDTO.success("신용평가 결과가 성공적으로 수정되었습니다.", response);
    }
    
    @Override
    public CommonResponseDTO<CreditEvaluationResultResponse> getCreditEvaluationResult(Long userId) {
        
        log.info("신용평가 결과 조회 시작: userId={}", userId);
        
        CreditEvaluationResult result = creditEvaluationResultMapper.selectCreditEvaluationResult(userId);
        
        if (result == null) {
            throw new CreditEvaluationNotFoundException(
                "해당 사용자의 신용평가 결과를 찾을 수 없습니다. userId: " + userId);
        }
        
        CreditEvaluationResultResponse response = convertToResponse(result);
        
        log.info("신용평가 결과 조회 완료: userId={}, totalScore={}", userId, response.getTotalScore());
        
        return CommonResponseDTO.success("신용평가 결과 조회가 완료되었습니다.", response);
    }
    
    @Override
    public CommonResponseDTO<List<CreditEvaluationResultResponse>> getCreditEvaluationResultList() {
        
        log.info("신용평가 결과 목록 조회 시작");
        
        List<CreditEvaluationResult> results = creditEvaluationResultMapper.selectCreditEvaluationResultList();
        
        List<CreditEvaluationResultResponse> responses = results.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        log.info("신용평가 결과 목록 조회 완료: 총 {}건", responses.size());
        
        return CommonResponseDTO.success(
            String.format("신용평가 결과 목록 조회가 완료되었습니다. (총 %d건)", responses.size()), responses);
    }
    
    @Override
    public CommonResponseDTO<List<CreditEvaluationResultResponse>> getCreditEvaluationResultByScoreRange(
            Integer minScore, Integer maxScore) {
        
        log.info("점수 범위별 신용평가 결과 조회 시작: minScore={}, maxScore={}", minScore, maxScore);
        
        if (minScore != null && maxScore != null && minScore > maxScore) {
            throw new CreditEvaluationValidationException("최소 점수가 최대 점수보다 클 수 없습니다.");
        }
        
        List<CreditEvaluationResult> results = creditEvaluationResultMapper
                .selectCreditEvaluationResultByScoreRange(minScore, maxScore);
        
        List<CreditEvaluationResultResponse> responses = results.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
        
        log.info("점수 범위별 신용평가 결과 조회 완료: 총 {}건", responses.size());
        
        return CommonResponseDTO.success(
            String.format("점수 범위별 신용평가 결과 조회가 완료되었습니다. (총 %d건)", responses.size()), responses);
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteCreditEvaluationResult(Long userId) {
        
        log.info("신용평가 결과 삭제 시작: userId={}", userId);
        
        // 존재 여부 체크
        if (!creditEvaluationResultMapper.existsCreditEvaluationResult(userId)) {
            throw new CreditEvaluationNotFoundException(
                "해당 사용자의 신용평가 결과를 찾을 수 없습니다. userId: " + userId);
        }
        
        // 데이터베이스 삭제
        int deletedRows = creditEvaluationResultMapper.deleteCreditEvaluationResult(userId);
        
        if (deletedRows == 0) {
            throw new CreditEvaluationValidationException("신용평가 결과 삭제에 실패했습니다.");
        }
        
        log.info("신용평가 결과 삭제 완료: userId={}", userId);
        
        return CommonResponseDTO.success("신용평가 결과가 성공적으로 삭제되었습니다.");
    }
    
    /**
     * Domain 객체를 Response DTO로 변환
     */
    private CreditEvaluationResultResponse convertToResponse(CreditEvaluationResult result) {
        return CreditEvaluationResultResponse.builder()
                .userId(result.getUserId())
                .totalScore(result.getTotalScore())
                .repaymentHistoryScore(result.getRepaymentHistoryScore())
                .debtLevelScore(result.getDebtLevelScore())
                .creditPeriodScore(result.getCreditPeriodScore())
                .creditPatternScore(result.getCreditPatternScore())
                .nonFinancialScore(result.getNonFinancialScore())
                .scoreCalculatedDttm(result.getScoreCalculatedDttm())
                .build();
    }
}