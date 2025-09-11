package com.guideon.document.mapper;

import com.guideon.document.domain.LoanSessionVO;
import com.guideon.document.dto.DocumentResultDTO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LoanSessionMapper {

    /**
     * 대출 세션 생성
     */
    int insert(LoanSessionVO loanSession);

    /**
     * 세션 ID로 조회
     */
    LoanSessionVO selectById(Long sessionId);

    /**
     * 세션 진행 상태 업데이트
     */
    void updateSessionProgress(@Param("sessionId") Long sessionId,
                               @Param("requiredDocuments") Integer requiredDocuments,
                               @Param("submittedDocuments") Integer submittedDocuments,
                               @Param("progressPercentage") Double progressPercentage);

    /**
     * business_id + policy_id로 기존 세션 조회 (자금 시뮬레이션 시작/재개용)
     */
    LoanSessionVO selectByBusinessIdAndPolicyId(@Param("businessId") Long businessId,
                                                @Param("policyId") Long policyId);

    /**
     * DocumentResult를 simulation_result 테이블에 저장
     */
    int insertDocumentResultToSimulation(@Param("sessionId") Long sessionId,
                                         @Param("memberId") Long memberId,
                                         @Param("documentResult") DocumentResultDTO documentResult);

    /**
     * DocumentResult를 simulation_result 테이블에 업데이트
     */
    int updateDocumentResultInSimulation(@Param("sessionId") Long sessionId,
                                         @Param("documentResult") DocumentResultDTO documentResult);

    /**
     * 시뮬레이션 결과의 현재 단계 조회
     */
    String selectSimulationCurrentStep(Long sessionId);
}
