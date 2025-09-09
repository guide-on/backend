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
     * 매장 요약 데이터 수정
     */
    int updateStoreSummary(StoreSummary storeSummary);
    
    /**
     * 특정 매장의 특정 년월 데이터 조회
     */
    StoreSummary selectStoreSummary(
            @Param("storeId") Long storeId,
            @Param("summaryYearMonth") String summaryYearMonth
    );
    
    /**
     * 특정 매장의 최신 요약 데이터 조회
     */
    StoreSummary selectLatestStoreSummary(@Param("storeId") Long storeId);
    
    /**
     * 매장 요약 데이터 목록 조회
     */
    List<StoreSummary> selectStoreSummaryList(
            @Param("storeId") Long storeId,
            @Param("ownerId") Long ownerId,
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("summaryYearMonth") String summaryYearMonth,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
    
    /**
     * 매장 요약 데이터 총 개수 조회
     */
    int countStoreSummaryList(
            @Param("storeId") Long storeId,
            @Param("ownerId") Long ownerId,
            @Param("businessRegistrationNo") String businessRegistrationNo,
            @Param("summaryYearMonth") String summaryYearMonth
    );
    
    /**
     * 특정 매장의 요약 이력 조회
     */
    List<StoreSummary> selectStoreSummaryHistory(
            @Param("storeId") Long storeId,
            @Param("limit") int limit,
            @Param("offset") int offset
    );
    
    /**
     * 특정 매장의 요약 이력 개수 조회
     */
    int countStoreSummaryHistory(@Param("storeId") Long storeId);
    
    /**
     * 매장 요약 데이터 삭제
     */
    int deleteStoreSummary(
            @Param("storeId") Long storeId,
            @Param("summaryYearMonth") String summaryYearMonth
    );
    
    /**
     * 특정 매장의 모든 요약 데이터 삭제
     */
    int deleteAllStoreSummaryByStoreId(@Param("storeId") Long storeId);
    
    /**
     * 매장 요약 데이터 존재 여부 확인
     */
    boolean existsStoreSummary(
            @Param("storeId") Long storeId,
            @Param("summaryYearMonth") String summaryYearMonth
    );
    
    /**
     * 사업주별 매장 요약 데이터 조회
     */
    List<StoreSummary> selectStoreSummaryByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("summaryYearMonth") String summaryYearMonth,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );
    
    /**
     * 사업주별 매장 요약 데이터 개수 조회
     */
    int countStoreSummaryByOwnerId(
            @Param("ownerId") Long ownerId,
            @Param("summaryYearMonth") String summaryYearMonth
    );
}