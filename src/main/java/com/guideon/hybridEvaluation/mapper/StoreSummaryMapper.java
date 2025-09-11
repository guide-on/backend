package com.guideon.hybridEvaluation.mapper;

import com.guideon.hybridEvaluation.domain.StoreSummary;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface StoreSummaryMapper {

    /**
     * 매장 요약 데이터 생성
     */
    int insertStoreSummary(StoreSummary storeSummary);

    /**
     * 매장 요약 데이터 수정 (매출 관련 필드만)
     */
    int updateStoreSummary(StoreSummary storeSummary);

    /**
     * 매장 요약 데이터 전체 수정 (모든 필드)
     */
    int updateStoreSummaryFull(StoreSummary storeSummary);

    /**
     * 특정 회원의 특정 데이터 조회
     */
    StoreSummary selectStoreSummary(
            @Param("sessionId") Long sessionId,
            @Param("businessRegistrationNo") String businessRegistrationNo
    );

    /**
     * 특정 회원의 최신 요약 데이터 조회
     */
    StoreSummary selectLatestStoreSummary(@Param("sessionId") Long sessionId);

    /**
     * 회원 요약 데이터 목록 조회
     */
    List<StoreSummary> selectStoreSummaryList(
            @Param("sessionId") Long sessionId,
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    /**
     * 회원 요약 데이터 총 개수 조회
     */
    int countStoreSummaryList(
            @Param("sessionId") Long sessionId,
            @Param("businessRegistrationNo") String businessRegistrationNo
    );

    /**
     * 특정 회원의 요약 이력 조회
     */
    List<StoreSummary> selectStoreSummaryHistory(
            @Param("sessionId") Long sessionId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    /**
     * 특정 회원의 요약 이력 개수 조회
     */
    int countStoreSummaryHistory(@Param("sessionId") Long sessionId);

    /**
     * 회원 요약 데이터 삭제
     */
    int deleteStoreSummary(
            @Param("sessionId") Long sessionId,
            @Param("businessRegistrationNo") String businessRegistrationNo
    );

    /**
     * 특정 회원의 모든 요약 데이터 삭제
     */
    int deleteAllStoreSummaryBySessionId(@Param("sessionId") Long sessionId);

    /**
     * 회원 요약 데이터 존재 여부 확인
     */
    boolean existsStoreSummary(
            @Param("sessionId") Long sessionId,
            @Param("businessRegistrationNo") String businessRegistrationNo
    );

    /**
     * 세션 ID별 매장 요약 데이터 조회
     */
    List<StoreSummary> selectStoreSummaryBySessionId(
            @Param("sessionId") Long sessionId,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
}