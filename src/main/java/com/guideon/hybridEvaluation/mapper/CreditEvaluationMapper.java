package com.guideon.hybridEvaluation.mapper;

import com.guideon.hybridEvaluation.domain.CreditEvaluation;
import com.guideon.hybridEvaluation.dto.CreditEvaluationListRequest;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.sql.Timestamp;
import java.util.List;

@Mapper
public interface CreditEvaluationMapper {
    
    /**
     * 신용평가 데이터 생성
     */
    int insertCreditEvaluation(CreditEvaluation creditEvaluation);
    
    /**
     * 신용평가 데이터 수정
     */
    int updateCreditEvaluation(CreditEvaluation creditEvaluation);
    
    /**
     * 특정 사용자의 특정 평가일자 데이터 조회
     */
    CreditEvaluation selectCreditEvaluation(
            @Param("sessionId") String sessionId, 
            @Param("evaluationDate") Timestamp evaluationDate
    );
    
    /**
     * 특정 사용자의 최신 신용평가 데이터 조회
     */
    CreditEvaluation selectLatestCreditEvaluation(@Param("sessionId") String sessionId);
    
    /**
     * 신용평가 데이터 목록 조회 (페이징)
     */
    List<CreditEvaluation> selectCreditEvaluationList(CreditEvaluationListRequest request);
    
    /**
     * 신용평가 데이터 총 개수 조회
     */
    int countCreditEvaluationList(CreditEvaluationListRequest request);
    
    /**
     * 특정 사용자의 신용평가 이력 조회
     */
    List<CreditEvaluation> selectCreditEvaluationHistory(
            @Param("sessionId") String sessionId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    
    /**
     * 특정 사용자의 신용평가 이력 개수 조회
     */
    int countCreditEvaluationHistory(@Param("sessionId") String sessionId);
    
    /**
     * 신용평가 데이터 삭제 (물리적 삭제)
     */
    int deleteCreditEvaluation(
            @Param("sessionId") String sessionId, 
            @Param("evaluationDate") Timestamp evaluationDate
    );
    
    /**
     * 특정 사용자의 모든 신용평가 데이터 삭제
     */
    int deleteAllCreditEvaluationBySessionId(@Param("sessionId") String sessionId);
    
    /**
     * 신용평가 데이터 존재 여부 확인
     */
    boolean existsCreditEvaluation(
            @Param("sessionId") String sessionId, 
            @Param("evaluationDate") Timestamp evaluationDate
    );
}
