package com.guideon.hybridEvaluation.mapper;

import com.guideon.hybridEvaluation.domain.CreditEvaluationResult;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CreditEvaluationResultMapper {
    
    /**
     * 신용평가 결과 생성
     * @param creditEvaluationResult 신용평가 결과 정보
     * @return 생성된 행 수
     */
    int insertCreditEvaluationResult(CreditEvaluationResult creditEvaluationResult);
    
    /**
     * 신용평가 결과 수정
     * @param creditEvaluationResult 수정할 신용평가 결과 정보
     * @return 수정된 행 수
     */
    int updateCreditEvaluationResult(CreditEvaluationResult creditEvaluationResult);
    
    /**
     * 특정 사용자의 신용평가 결과 조회
     * @param userId 사용자 ID
     * @return 신용평가 결과
     */
    CreditEvaluationResult selectCreditEvaluationResult(@Param("userId") Long userId);
    
    /**
     * 모든 신용평가 결과 목록 조회
     * @return 신용평가 결과 목록
     */
    List<CreditEvaluationResult> selectCreditEvaluationResultList();
    
    /**
     * 점수 범위별 신용평가 결과 조회
     * @param minScore 최소 점수
     * @param maxScore 최대 점수
     * @return 해당 점수 범위의 신용평가 결과 목록
     */
    List<CreditEvaluationResult> selectCreditEvaluationResultByScoreRange(
            @Param("minScore") Integer minScore, 
            @Param("maxScore") Integer maxScore);
    
    /**
     * 신용평가 결과 삭제
     * @param userId 사용자 ID
     * @return 삭제된 행 수
     */
    int deleteCreditEvaluationResult(@Param("userId") Long userId);
    
    /**
     * 신용평가 결과 존재 여부 확인
     * @param userId 사용자 ID
     * @return 존재 여부
     */
    boolean existsCreditEvaluationResult(@Param("userId") Long userId);
    
    /**
     * 전체 신용평가 결과 개수 조회
     * @return 전체 개수
     */
    int countCreditEvaluationResults();
}