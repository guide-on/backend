package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.BadRequestException;
import com.guideon.common.exception.NotFoundException;
import com.guideon.hybridEvaluation.domain.CreditEvaluation;
import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import com.guideon.hybridEvaluation.dto.CreditEvaluationCreateRequest;
import com.guideon.hybridEvaluation.dto.CreditEvaluationListRequest;
import com.guideon.hybridEvaluation.dto.CreditEvaluationResponse;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationMapper;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CreditEvaluationServiceImpl implements CreditEvaluationService {
    
    private final CreditEvaluationMapper creditEvaluationMapper;
    private final CreditEvaluationResultMapper creditEvaluationResultMapper;
    private final CreditScoreCalculationService creditScoreCalculationService;
    
    @Override
    @Transactional
    public CommonResponseDTO<CreditEvaluationResponse> createCreditEvaluation(CreditEvaluationCreateRequest request) {
        try {
            // 유효성 검증
            validateCreateRequest(request);
            
            // 요청에서 받은 sessionId 사용
            Long sessionId = request.getSessionId();
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            // DTO를 Entity로 변환
            CreditEvaluation creditEvaluation = new CreditEvaluation();
            BeanUtils.copyProperties(request, creditEvaluation);
            
            // 요청에서 받은 sessionId를 설정
            creditEvaluation.setSessionId(sessionId);
            creditEvaluation.setEvaluationDate(new Timestamp(System.currentTimeMillis()));
            
            // 데이터베이스에 저장
            log.info("저장할 creditEvaluation: sessionId={}, evaluationDate={}", 
                     creditEvaluation.getSessionId(), creditEvaluation.getEvaluationDate());
            
            int result = creditEvaluationMapper.insertCreditEvaluation(creditEvaluation);
            log.info("INSERT 결과: {}", result);
            
            if (result > 0) {
                // 저장된 데이터 조회 (최신 데이터 조회로 변경)
                CreditEvaluation savedData = creditEvaluationMapper.selectLatestCreditEvaluation(
                    creditEvaluation.getSessionId()
                );
                
                log.info("조회된 savedData: {}", savedData);
                
                CreditEvaluationResponse response;
                
                if (savedData != null) {
                    // 신용점수 계산 및 저장
                    try {
                        CreditEvaluationResult scoreResult = creditScoreCalculationService.calculateCreditScore(savedData);
                        
                        // 기존 결과가 있다면 삭제 후 새로 저장
                        if (creditEvaluationResultMapper.existsCreditEvaluationResult(scoreResult.getSessionId())) {
                            creditEvaluationResultMapper.deleteCreditEvaluationResult(scoreResult.getSessionId());
                            log.info("기존 신용점수 결과 삭제됨: sessionId={}", scoreResult.getSessionId());
                        }
                        
                        creditEvaluationResultMapper.insertCreditEvaluationResult(scoreResult);
                        log.info("신용점수 계산 및 저장 완료: sessionId={}, totalScore={}", 
                                scoreResult.getSessionId(), scoreResult.getTotalScore());
                        
                    } catch (Exception scoreException) {
                        log.error("신용점수 계산 중 오류 발생: {}", scoreException.getMessage(), scoreException);
                        // 신용점수 계산 실패해도 신용평가 생성은 성공으로 처리
                    }
                    
                    // Entity를 Response DTO로 변환
                    response = convertToResponse(savedData);
                } else {
                    log.warn("저장된 데이터 조회 실패. 기본 응답 생성: sessionId={}", creditEvaluation.getSessionId());
                    // savedData가 null이면 기본 응답 생성
                    response = createDefaultResponse(creditEvaluation);
                }
                
                return CommonResponseDTO.success("신용평가 데이터가 성공적으로 생성되었습니다.", response);
            } else {
                throw new BadRequestException("신용평가 데이터 생성에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("신용평가 데이터 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("신용평가 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationList(CreditEvaluationListRequest request) {
        try {
            // 세션 ID 검증
            Long sessionId = request.getSessionId();
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }

            List<CreditEvaluation> creditEvaluations = creditEvaluationMapper.selectCreditEvaluationList(request);
            int totalCount = creditEvaluationMapper.countCreditEvaluationList(request);

            List<CreditEvaluationResponse> responses = creditEvaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

            return CommonResponseDTO.success(
                String.format("신용평가 데이터 목록 조회가 완료되었습니다. (총 %d건)", totalCount),
                responses
            );

        } catch (Exception e) {
            log.error("신용평가 데이터 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("신용평가 데이터 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    /**
     * Entity를 Response DTO로 변환
     */
    private CreditEvaluationResponse convertToResponse(CreditEvaluation creditEvaluation) {
        CreditEvaluationResponse response = new CreditEvaluationResponse();
        BeanUtils.copyProperties(creditEvaluation, response);
        return response;
    }
    
    /**
     * 기본 응답 생성 (savedData 조회 실패 시)
     */
    private CreditEvaluationResponse createDefaultResponse(CreditEvaluation creditEvaluation) {
        CreditEvaluationResponse response = new CreditEvaluationResponse();
        response.setSessionId(creditEvaluation.getSessionId());
        response.setEvaluationDate(creditEvaluation.getEvaluationDate());
        response.setTotalOverdueCount(creditEvaluation.getTotalOverdueCount());
        response.setRecent12mOverdueCount(creditEvaluation.getRecent12mOverdueCount());
        response.setMaxOverdueDays(creditEvaluation.getMaxOverdueDays());
        response.setCurrentOverdueAmount(creditEvaluation.getCurrentOverdueAmount());
        response.setLoanDefaultHistory(creditEvaluation.getLoanDefaultHistory());
        response.setCreditCardDelayRate(creditEvaluation.getCreditCardDelayRate());
        response.setPaymentConsistencyScore(creditEvaluation.getPaymentConsistencyScore());
        response.setTotalDebtAmount(creditEvaluation.getTotalDebtAmount());
        response.setMonthlyIncome(creditEvaluation.getMonthlyIncome());
        response.setDebtToIncomeRatio(creditEvaluation.getDebtToIncomeRatio());
        response.setCreditCardUtilizationRate(creditEvaluation.getCreditCardUtilizationRate());
        response.setSecuredVsUnsecuredRatio(creditEvaluation.getSecuredVsUnsecuredRatio());
        response.setCreditHistoryMonths(creditEvaluation.getCreditHistoryMonths());
        response.setOldestCreditAccountMonths(creditEvaluation.getOldestCreditAccountMonths());
        response.setNewCreditInquiries6m(creditEvaluation.getNewCreditInquiries6m());
        response.setActiveCreditCardCount(creditEvaluation.getActiveCreditCardCount());
        response.setTotalCreditLimit(creditEvaluation.getTotalCreditLimit());
        response.setLoanTypeDiversity(creditEvaluation.getLoanTypeDiversity());
        response.setFinancialInstitutionCount(creditEvaluation.getFinancialInstitutionCount());
        response.setAlternativeCreditScore(creditEvaluation.getAlternativeCreditScore());
        return response;
    }
    
    /**
     * 생성 요청 유효성 검증
     */
    private void validateCreateRequest(CreditEvaluationCreateRequest request) {
        // sessionId는 요청에서 제공되므로 이미 검증됨
        
        // 추가적인 비즈니스 로직 검증
        validateBusinessRules(request);
    }
    
    /**
     * 비즈니스 규칙 검증
     */
    private void validateBusinessRules(CreditEvaluationCreateRequest request) {
        // 신용카드 연체율 검증 (0-100%)
        if (request.getCreditCardDelayRate() != null) {
            if (request.getCreditCardDelayRate().compareTo(java.math.BigDecimal.ZERO) < 0 || 
                request.getCreditCardDelayRate().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new BadRequestException("신용카드 연체율은 0-100% 범위여야 합니다.");
            }
        }
        
        // 납부 일관성 점수 검증 (0-100)
        if (request.getPaymentConsistencyScore() != null) {
            if (request.getPaymentConsistencyScore() < 0 || request.getPaymentConsistencyScore() > 100) {
                throw new BadRequestException("납부 일관성 점수는 0-100 범위여야 합니다.");
            }
        }
        
        // 신용카드 이용률 검증 (0-100%)
        if (request.getCreditCardUtilizationRate() != null) {
            if (request.getCreditCardUtilizationRate().compareTo(java.math.BigDecimal.ZERO) < 0 || 
                request.getCreditCardUtilizationRate().compareTo(new java.math.BigDecimal("100")) > 0) {
                throw new BadRequestException("신용카드 이용률은 0-100% 범위여야 합니다.");
            }
        }
        
        // 대출상품 다양성 검증 (0-5)
        if (request.getLoanTypeDiversity() != null) {
            if (request.getLoanTypeDiversity() < 0 || request.getLoanTypeDiversity() > 5) {
                throw new BadRequestException("대출상품 다양성은 0-5 범위여야 합니다.");
            }
        }
        
        // 대안신용점수 검증 (0-100)
        if (request.getAlternativeCreditScore() != null) {
            if (request.getAlternativeCreditScore() < 0 || request.getAlternativeCreditScore() > 100) {
                throw new BadRequestException("대안신용점수는 0-100 범위여야 합니다.");
            }
        }
        
        // 대출 부도이력 검증 (0 또는 1)
        if (request.getLoanDefaultHistory() != null) {
            if (request.getLoanDefaultHistory() != 0 && request.getLoanDefaultHistory() != 1) {
                throw new BadRequestException("대출 부도이력은 0(없음) 또는 1(있음)이어야 합니다.");
            }
        }
        
        // 음수값 검증
        validatePositiveNumbers(request);
    }
    
    /**
     * 양수값 검증
     */
    private void validatePositiveNumbers(CreditEvaluationCreateRequest request) {
        if (request.getTotalDebtAmount() != null && 
            request.getTotalDebtAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("총 부채금액은 0 이상이어야 합니다.");
        }
        
        if (request.getMonthlyIncome() != null && 
            request.getMonthlyIncome().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("월소득은 0 이상이어야 합니다.");
        }
        
        if (request.getCurrentOverdueAmount() != null && 
            request.getCurrentOverdueAmount().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("현재 연체금액은 0 이상이어야 합니다.");
        }
        
        if (request.getTotalCreditLimit() != null && 
            request.getTotalCreditLimit().compareTo(java.math.BigDecimal.ZERO) < 0) {
            throw new BadRequestException("총 신용한도는 0 이상이어야 합니다.");
        }
        
        // 개수값들 검증
        if (request.getTotalOverdueCount() != null && request.getTotalOverdueCount() < 0) {
            throw new BadRequestException("총 연체 횟수는 0 이상이어야 합니다.");
        }
        
        if (request.getRecent12mOverdueCount() != null && request.getRecent12mOverdueCount() < 0) {
            throw new BadRequestException("최근 12개월 연체 횟수는 0 이상이어야 합니다.");
        }
        
        if (request.getMaxOverdueDays() != null && request.getMaxOverdueDays() < 0) {
            throw new BadRequestException("최대 연체일수는 0 이상이어야 합니다.");
        }
        
        if (request.getCreditHistoryMonths() != null && request.getCreditHistoryMonths() < 0) {
            throw new BadRequestException("신용거래 총 개월수는 0 이상이어야 합니다.");
        }
        
        if (request.getOldestCreditAccountMonths() != null && request.getOldestCreditAccountMonths() < 0) {
            throw new BadRequestException("최초 신용거래 경과월수는 0 이상이어야 합니다.");
        }
        
        if (request.getNewCreditInquiries6m() != null && request.getNewCreditInquiries6m() < 0) {
            throw new BadRequestException("최근 6개월 신용조회 횟수는 0 이상이어야 합니다.");
        }
        
        if (request.getActiveCreditCardCount() != null && request.getActiveCreditCardCount() < 0) {
            throw new BadRequestException("활성 신용카드 수는 0 이상이어야 합니다.");
        }
        
        if (request.getFinancialInstitutionCount() != null && request.getFinancialInstitutionCount() < 0) {
            throw new BadRequestException("거래 금융기관 수는 0 이상이어야 합니다.");
        }
    }
}