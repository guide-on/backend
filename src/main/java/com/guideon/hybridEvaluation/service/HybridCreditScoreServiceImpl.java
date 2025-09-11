package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.domain.MemberCredit;
import com.guideon.hybridEvaluation.dto.HybridCreditScoreResponse;
import com.guideon.hybridEvaluation.mapper.CreditEvaluationResultMapper;
import com.guideon.hybridEvaluation.mapper.MemberCreditMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class HybridCreditScoreServiceImpl implements HybridCreditScoreService {
    
    private final MemberCreditMapper memberCreditMapper;
    private final CreditEvaluationResultMapper creditEvaluationResultMapper;
    
    @Override
    @Transactional(readOnly = true)
    public HybridCreditScoreResponse getHybridCreditScore(Long sessionId) {
        log.info("하이브리드 신용점수 조회 - sessionId: {}", sessionId);
        
        MemberCredit memberCredit = memberCreditMapper.findBySessionId(sessionId);
        if (memberCredit == null) {
            throw new RuntimeException("하이브리드 신용점수를 찾을 수 없습니다. sessionId: " + sessionId);
        }
        
        return convertToResponse(memberCredit);
    }
    
    @Override
    public HybridCreditScoreResponse calculateHybridCreditScore(Long sessionId) {
        log.info("하이브리드 신용점수 계산 시작 - sessionId: {}", sessionId);
        
        // TODO: 실제 하이브리드 신용점수 계산 로직 구현
        // 현재는 더미 데이터로 구현
        MemberCredit memberCredit = MemberCredit.builder()
                .sessionId(sessionId)
                .totalCreditScore(1000)
                .hybridCreditScore(1000)
                .traditionalCreditScore(null) // 계산시에는 null
                .salesSummaryScoreScaled(229)
                .financialInfoScoreScaled(264)
                .operationalInfoScoreScaled(223)
                .lastUpdatedDttm(LocalDateTime.now())
                .build();
        
        // 기존 데이터가 있는지 확인 후 insert 또는 update 실행
        MemberCredit existing = memberCreditMapper.findBySessionId(sessionId);
        if (existing == null) {
            memberCreditMapper.insert(memberCredit);
        } else {
            memberCreditMapper.update(memberCredit);
        }
        
        log.info("하이브리드 신용점수 계산 완료 - sessionId: {}", sessionId);
        return convertToResponse(memberCredit);
    }
    
    @Override
    @Transactional(readOnly = true)
    public HybridCreditScoreResponse getHybridCreditScoreResult(Long sessionId) {
        log.info("하이브리드 신용점수 결과 조회 - sessionId: {}", sessionId);
        
        MemberCredit memberCredit = memberCreditMapper.findBySessionId(sessionId);
        if (memberCredit == null) {
            throw new RuntimeException("하이브리드 신용점수 결과를 찾을 수 없습니다. sessionId: " + sessionId);
        }
        
        return convertToResponse(memberCredit);
    }
    
    @Override
    public HybridCreditScoreResponse updateTraditionalCreditScore(Long sessionId) {
        log.info("Traditional Credit Score 업데이트 시작 - sessionId: {}", sessionId);
        
        // credit_evaluation_result 테이블에서 total_score 조회
        var creditResult = creditEvaluationResultMapper.selectCreditEvaluationResult(sessionId);
        if (creditResult == null) {
            throw new RuntimeException("신용평가 결과를 찾을 수 없습니다. sessionId: " + sessionId);
        }
        
        Integer totalScore = creditResult.getTotalScore();
        if (totalScore == null) {
            throw new RuntimeException("신용평가 총점이 없습니다. sessionId: " + sessionId);
        }
        
        // member_credit 테이블의 traditional_credit_score 업데이트
        int updatedRows = memberCreditMapper.updateTraditionalCreditScore(sessionId, totalScore);
        if (updatedRows == 0) {
            throw new RuntimeException("Traditional Credit Score 업데이트 실패. sessionId: " + sessionId);
        }
        
        log.info("Traditional Credit Score 업데이트 완료 - sessionId: {}, score: {}", sessionId, totalScore);
        
        // total_credit_score 계산 및 업데이트 (3:7 비율)
        calculateAndUpdateTotalCreditScore(sessionId);
        
        // 업데이트된 데이터 반환
        MemberCredit memberCredit = memberCreditMapper.findBySessionId(sessionId);
        return convertToResponse(memberCredit);
    }
    
    @Override
    public HybridCreditScoreResponse calculateAndUpdateTotalCreditScore(Long sessionId) {
        log.info("Total Credit Score 계산 및 업데이트 시작 - sessionId: {}", sessionId);
        
        // member_credit 데이터가 존재하는지 확인
        MemberCredit memberCredit = memberCreditMapper.findBySessionId(sessionId);
        if (memberCredit == null) {
            throw new RuntimeException("회원 신용정보를 찾을 수 없습니다. sessionId: " + sessionId);
        }
        
        // hybrid_credit_score와 traditional_credit_score가 모두 null인 경우 예외 처리
        if (memberCredit.getHybridCreditScore() == null && memberCredit.getTraditionalCreditScore() == null) {
            throw new RuntimeException("계산에 필요한 점수 데이터가 없습니다. sessionId: " + sessionId);
        }
        
        // 3:7 비율로 계산해서 업데이트
        int updatedRows = memberCreditMapper.calculateAndUpdateTotalCreditScore(sessionId);
        if (updatedRows == 0) {
            throw new RuntimeException("Total Credit Score 업데이트 실패. sessionId: " + sessionId);
        }
        
        // 업데이트된 데이터 조회
        MemberCredit updatedMemberCredit = memberCreditMapper.findBySessionId(sessionId);
        
        log.info("Total Credit Score 계산 완료 - sessionId: {}, hybrid: {}, traditional: {}, total: {}", 
                sessionId, 
                updatedMemberCredit.getHybridCreditScore(), 
                updatedMemberCredit.getTraditionalCreditScore(),
                updatedMemberCredit.getTotalCreditScore());
        
        return convertToResponse(updatedMemberCredit);
    }
    
    private HybridCreditScoreResponse convertToResponse(MemberCredit memberCredit) {
        return HybridCreditScoreResponse.builder()
                .sessionId(memberCredit.getSessionId())
                .totalCreditScore(memberCredit.getTotalCreditScore())
                .hybridCreditScore(memberCredit.getHybridCreditScore())
                .traditionalCreditScore(memberCredit.getTraditionalCreditScore())
                .lastUpdatedDttm(memberCredit.getLastUpdatedDttm())
                .salesSummaryScoreScaled(memberCredit.getSalesSummaryScoreScaled())
                .financialInfoScoreScaled(memberCredit.getFinancialInfoScoreScaled())
                .operationalInfoScoreScaled(memberCredit.getOperationalInfoScoreScaled())
                .build();
    }
}