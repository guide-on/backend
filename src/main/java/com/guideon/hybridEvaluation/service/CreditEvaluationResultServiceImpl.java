package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResultResponse;
import com.guideon.hybridEvaluation.exception.CreditEvaluationNotFoundException;
import com.guideon.hybridEvaluation.exception.CreditEvaluationValidationException;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.guideon.common.exception.BadRequestException;

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
    public CommonResponseDTO<CreditEvaluationResultResponse> getCreditEvaluationResult(Long sessionId) {

        log.info("신용평가 결과 조회 시작: sessionId={}", sessionId);

        // 세션 ID 검증
        if (sessionId == null) {
            throw new BadRequestException("세션 ID는 필수 값입니다.");
        }

        CreditEvaluationResult result = creditEvaluationResultMapper.selectCreditEvaluationResult(sessionId);

        if (result == null) {
            throw new CreditEvaluationNotFoundException(
                "해당 사용자의 신용평가 결과를 찾을 수 없습니다. sessionId: " + sessionId);
        }

        CreditEvaluationResultResponse response = convertToResponse(result);

        log.info("신용평가 결과 조회 완료: sessionId={}, totalScore={}", sessionId, response.getTotalScore());

        return CommonResponseDTO.success("신용평가 결과 조회가 완료되었습니다.", response);
    }
    
    /**
     * Domain 객체를 Response DTO로 변환
     */
    private CreditEvaluationResultResponse convertToResponse(CreditEvaluationResult result) {
        return CreditEvaluationResultResponse.builder()
                .sessionId(result.getSessionId())
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