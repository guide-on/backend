package com.guideon.hybridEvaluation.mapper;

import com.guideon.hybridEvaluation.domain.MemberCredit;
import org.apache.ibatis.annotations.*;

@Mapper
public interface MemberCreditMapper {
    
    /**
     * 하이브리드 신용점수 조회
     */
    @Select("SELECT session_id as sessionId, " +
            "total_credit_score as totalCreditScore, " +
            "hybrid_credit_score as hybridCreditScore, " +
            "traditional_credit_score as traditionalCreditScore, " +
            "last_updated_dttm as lastUpdatedDttm, " +
            "sales_summary_score_scaled as salesSummaryScoreScaled, " +
            "financial_info_score_scaled as financialInfoScoreScaled, " +
            "operational_info_score_scaled as operationalInfoScoreScaled " +
            "FROM member_credit WHERE session_id = #{sessionId}")
    MemberCredit findBySessionId(@Param("sessionId") Long sessionId);
    
    /**
     * 하이브리드 신용점수 생성
     */
    @Insert("INSERT INTO member_credit (session_id, total_credit_score, hybrid_credit_score, traditional_credit_score, " +
            "last_updated_dttm, sales_summary_score_scaled, financial_info_score_scaled, operational_info_score_scaled) " +
            "VALUES (#{sessionId}, #{totalCreditScore}, #{hybridCreditScore}, #{traditionalCreditScore}, " +
            "NOW(), #{salesSummaryScoreScaled}, #{financialInfoScoreScaled}, #{operationalInfoScoreScaled})")
    int insert(MemberCredit memberCredit);
    
    /**
     * 하이브리드 신용점수 업데이트
     */
    @Update("UPDATE member_credit SET " +
            "total_credit_score = #{totalCreditScore}, " +
            "hybrid_credit_score = #{hybridCreditScore}, " +
            "traditional_credit_score = #{traditionalCreditScore}, " +
            "last_updated_dttm = NOW(), " +
            "sales_summary_score_scaled = #{salesSummaryScoreScaled}, " +
            "financial_info_score_scaled = #{financialInfoScoreScaled}, " +
            "operational_info_score_scaled = #{operationalInfoScoreScaled} " +
            "WHERE session_id = #{sessionId}")
    int update(MemberCredit memberCredit);
    
    /**
     * traditional_credit_score 업데이트
     */
    @Update("UPDATE member_credit SET traditional_credit_score = #{traditionalCreditScore}, " +
            "last_updated_dttm = NOW() WHERE session_id = #{sessionId}")
    int updateTraditionalCreditScore(@Param("sessionId") Long sessionId, 
                                   @Param("traditionalCreditScore") Integer traditionalCreditScore);
    
    /**
     * total_credit_score 업데이트
     */
    @Update("UPDATE member_credit SET total_credit_score = #{totalCreditScore}, " +
            "last_updated_dttm = NOW() WHERE session_id = #{sessionId}")
    int updateTotalCreditScore(@Param("sessionId") Long sessionId, 
                             @Param("totalCreditScore") Integer totalCreditScore);
    
    /**
     * total_credit_score를 hybrid_credit_score와 traditional_credit_score의 3:7 비율로 계산해서 업데이트
     */
    @Update("UPDATE member_credit SET " +
            "total_credit_score = ROUND(COALESCE(hybrid_credit_score, 0) * 0.3 + COALESCE(traditional_credit_score, 0) * 0.7), " +
            "last_updated_dttm = NOW() " +
            "WHERE session_id = #{sessionId} " +
            "AND (hybrid_credit_score IS NOT NULL OR traditional_credit_score IS NOT NULL)")
    int calculateAndUpdateTotalCreditScore(@Param("sessionId") Long sessionId);
}