package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.BadRequestException;
import com.guideon.common.exception.NotFoundException;
import com.guideon.hybridEvaluation.domain.CreditEvaluation;
import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import com.guideon.hybridEvaluation.dto.*;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationMapper;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationResultMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.guideon.security.util.LoginUserProvider;

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
    private final LoginUserProvider loginUserProvider;
    
    @Override
    @Transactional
    public CommonResponseDTO<CreditEvaluationResponse> createCreditEvaluation(CreditEvaluationCreateRequest request) {
        try {
            // 로그인한 사용자의 memberId 가져오기
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 유효성 검증
            validateCreateRequest(request);
            
            // DTO를 Entity로 변환
            CreditEvaluation creditEvaluation = new CreditEvaluation();
            BeanUtils.copyProperties(request, creditEvaluation);
            
            // 로그인한 사용자의 memberId를 설정 (request에서 받지 않고 직접 설정)
            creditEvaluation.setMemberId(String.valueOf(loginMemberId));
            creditEvaluation.setEvaluationDate(new Timestamp(System.currentTimeMillis()));
            
            // 데이터베이스에 저장
            log.info("저장할 creditEvaluation: memberId={}, evaluationDate={}", 
                     creditEvaluation.getMemberId(), creditEvaluation.getEvaluationDate());
            
            int result = creditEvaluationMapper.insertCreditEvaluation(creditEvaluation);
            log.info("INSERT 결과: {}", result);
            
            if (result > 0) {
                // 저장된 데이터 조회 (최신 데이터 조회로 변경)
                CreditEvaluation savedData = creditEvaluationMapper.selectLatestCreditEvaluation(
                    creditEvaluation.getMemberId()
                );
                
                log.info("조회된 savedData: {}", savedData);
                
                CreditEvaluationResponse response;
                
                if (savedData != null) {
                    // 신용점수 계산 및 저장
                    try {
                        CreditEvaluationResult scoreResult = creditScoreCalculationService.calculateCreditScore(savedData);
                        
                        // 기존 결과가 있다면 삭제 후 새로 저장
                        if (creditEvaluationResultMapper.existsCreditEvaluationResult(scoreResult.getMemberId())) {
                            creditEvaluationResultMapper.deleteCreditEvaluationResult(scoreResult.getMemberId());
                            log.info("기존 신용점수 결과 삭제됨: memberId={}", scoreResult.getMemberId());
                        }
                        
                        creditEvaluationResultMapper.insertCreditEvaluationResult(scoreResult);
                        log.info("신용점수 계산 및 저장 완료: memberId={}, totalScore={}", 
                                scoreResult.getMemberId(), scoreResult.getTotalScore());
                        
                    } catch (Exception scoreException) {
                        log.error("신용점수 계산 중 오류 발생: {}", scoreException.getMessage(), scoreException);
                        // 신용점수 계산 실패해도 신용평가 생성은 성공으로 처리
                    }
                    
                    // Entity를 Response DTO로 변환
                    response = convertToResponse(savedData);
                } else {
                    log.warn("저장된 데이터 조회 실패. 기본 응답 생성: memberId={}", creditEvaluation.getMemberId());
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
    @Transactional
    public CommonResponseDTO<CreditEvaluationResponse> updateCreditEvaluation(CreditEvaluationUpdateRequest request) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 수정할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(request.getMemberId())) {
                throw new BadRequestException("본인의 데이터만 수정할 수 있습니다.");
            }
            
            // 유효성 검증
            validateUpdateRequest(request);
            
            // 기존 데이터 존재 여부 확인
            boolean exists = creditEvaluationMapper.existsCreditEvaluation(
                request.getMemberId(), 
                request.getEvaluationDate()
            );
            
            if (!exists) {
                throw new NotFoundException("수정할 신용평가 데이터를 찾을 수 없습니다.");
            }
            
            // DTO를 Entity로 변환
            CreditEvaluation creditEvaluation = new CreditEvaluation();
            BeanUtils.copyProperties(request, creditEvaluation);
            
            // 데이터베이스 업데이트
            int result = creditEvaluationMapper.updateCreditEvaluation(creditEvaluation);
            
            if (result > 0) {
                // 수정된 데이터 조회
                CreditEvaluation updatedData = creditEvaluationMapper.selectCreditEvaluation(
                    creditEvaluation.getMemberId(), 
                    creditEvaluation.getEvaluationDate()
                );
                
                // 신용점수 재계산 및 저장
                try {
                    CreditEvaluationResult scoreResult = creditScoreCalculationService.calculateCreditScore(updatedData);
                    
                    // 기존 결과 업데이트 또는 새로 생성
                    if (creditEvaluationResultMapper.existsCreditEvaluationResult(scoreResult.getMemberId())) {
                        creditEvaluationResultMapper.updateCreditEvaluationResult(scoreResult);
                        log.info("신용점수 결과 업데이트 완료: memberId={}, totalScore={}", 
                                scoreResult.getMemberId(), scoreResult.getTotalScore());
                    } else {
                        creditEvaluationResultMapper.insertCreditEvaluationResult(scoreResult);
                        log.info("신용점수 결과 새로 생성 완료: memberId={}, totalScore={}", 
                                scoreResult.getMemberId(), scoreResult.getTotalScore());
                    }
                    
                } catch (Exception scoreException) {
                    log.error("신용점수 재계산 중 오류 발생: {}", scoreException.getMessage(), scoreException);
                    // 신용점수 계산 실패해도 신용평가 수정은 성공으로 처리
                }
                
                // Entity를 Response DTO로 변환
                CreditEvaluationResponse response = convertToResponse(updatedData);
                
                return CommonResponseDTO.success("신용평가 데이터가 성공적으로 수정되었습니다.", response);
            } else {
                throw new BadRequestException("신용평가 데이터 수정에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("신용평가 데이터 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("신용평가 데이터 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommonResponseDTO<CreditEvaluationResponse> getCreditEvaluation(String memberId, Timestamp evaluationDate) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 조회할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(memberId)) {
                throw new BadRequestException("본인의 데이터만 조회할 수 있습니다.");
            }
            
            CreditEvaluation creditEvaluation = creditEvaluationMapper.selectCreditEvaluation(memberId, evaluationDate);
            
            if (creditEvaluation == null) {
                throw new NotFoundException("해당 신용평가 데이터를 찾을 수 없습니다.");
            }
            
            CreditEvaluationResponse response = convertToResponse(creditEvaluation);
            return CommonResponseDTO.success("신용평가 데이터 조회가 완료되었습니다.", response);
            
        } catch (Exception e) {
            log.error("신용평가 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("신용평가 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommonResponseDTO<CreditEvaluationResponse> getLatestCreditEvaluation(String memberId) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 조회할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(memberId)) {
                throw new BadRequestException("본인의 데이터만 조회할 수 있습니다.");
            }
            
            CreditEvaluation creditEvaluation = creditEvaluationMapper.selectLatestCreditEvaluation(memberId);
            
            if (creditEvaluation == null) {
                throw new NotFoundException("해당 사용자의 신용평가 데이터를 찾을 수 없습니다.");
            }
            
            CreditEvaluationResponse response = convertToResponse(creditEvaluation);
            return CommonResponseDTO.success("최신 신용평가 데이터 조회가 완료되었습니다.", response);
            
        } catch (Exception e) {
            log.error("최신 신용평가 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("최신 신용평가 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationList(CreditEvaluationListRequest request) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 조회할 수 있도록 강제 설정
            request.setMemberId(String.valueOf(loginMemberId));
            
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
    
    @Override
    @Transactional(readOnly = true)
    public CommonResponseDTO<List<CreditEvaluationResponse>> getCreditEvaluationHistory(String memberId, Integer page, Integer limit) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 조회할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(memberId)) {
                throw new BadRequestException("본인의 데이터만 조회할 수 있습니다.");
            }
            
            int offset = (page - 1) * limit;
            List<CreditEvaluation> creditEvaluations = creditEvaluationMapper.selectCreditEvaluationHistory(memberId, limit, offset);
            int totalCount = creditEvaluationMapper.countCreditEvaluationHistory(memberId);
            
            List<CreditEvaluationResponse> responses = creditEvaluations.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success(
                String.format("사용자 신용평가 이력 조회가 완료되었습니다. (총 %d건)", totalCount), 
                responses
            );
            
        } catch (Exception e) {
            log.error("사용자 신용평가 이력 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("사용자 신용평가 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteCreditEvaluation(String memberId, Timestamp evaluationDate) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 삭제할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(memberId)) {
                throw new BadRequestException("본인의 데이터만 삭제할 수 있습니다.");
            }
            
            // 기존 데이터 존재 여부 확인
            boolean exists = creditEvaluationMapper.existsCreditEvaluation(memberId, evaluationDate);
            
            if (!exists) {
                throw new NotFoundException("삭제할 신용평가 데이터를 찾을 수 없습니다.");
            }
            
            int result = creditEvaluationMapper.deleteCreditEvaluation(memberId, evaluationDate);
            
            if (result > 0) {
                return CommonResponseDTO.success("신용평가 데이터가 성공적으로 삭제되었습니다.", null);
            } else {
                throw new BadRequestException("신용평가 데이터 삭제에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("신용평가 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("신용평가 데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteAllCreditEvaluationByMemberId(String memberId) {
        try {
            // 로그인한 사용자의 memberId 검증
            Long loginMemberId = loginUserProvider.getLoginMemberId();
            if (loginMemberId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            // 본인의 데이터만 삭제할 수 있도록 검증
            if (!String.valueOf(loginMemberId).equals(memberId)) {
                throw new BadRequestException("본인의 데이터만 삭제할 수 있습니다.");
            }
            
            int result = creditEvaluationMapper.deleteAllCreditEvaluationByMemberId(memberId);
            
            return CommonResponseDTO.success(
                String.format("사용자의 모든 신용평가 데이터가 삭제되었습니다. (삭제된 건수: %d)", result), 
                null
            );
            
        } catch (Exception e) {
            log.error("사용자 신용평가 데이터 전체 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("사용자 신용평가 데이터 전체 삭제 중 오류가 발생했습니다: " + e.getMessage());
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
        response.setMemberId(creditEvaluation.getMemberId());
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
        // memberId는 loginUserProvider에서 자동으로 설정되므로 검증하지 않음
        
        // 추가적인 비즈니스 로직 검증
        validateBusinessRules(request);
    }
    
    /**
     * 수정 요청 유효성 검증
     */
    private void validateUpdateRequest(CreditEvaluationUpdateRequest request) {
        if (request.getMemberId() == null || request.getMemberId().trim().isEmpty()) {
            throw new BadRequestException("사용자 ID는 필수입니다.");
        }
        
        if (request.getEvaluationDate() == null) {
            throw new BadRequestException("평가일자는 필수입니다.");
        }
        
        // 추가적인 비즈니스 로직 검증 (CreateRequest와 동일한 검증 로직 사용)
        CreditEvaluationCreateRequest createRequest = new CreditEvaluationCreateRequest();
        BeanUtils.copyProperties(request, createRequest);
        validateBusinessRules(createRequest);
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
