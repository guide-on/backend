package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.dto.*;
import com.guideon.common.dto.CommonResponseDTO;

import java.util.List;

public interface StoreSummaryService {
    
    /**
     * 매장 요약 데이터 생성
     */
    CommonResponseDTO<StoreSummaryResponse> createStoreSummary(StoreSummaryCreateRequest request);
    
    /**
     * 매장 요약 데이터 수정
     */
    CommonResponseDTO<StoreSummaryResponse> updateStoreSummary(StoreSummaryUpdateRequest request);
    
    /**
     * 특정 회원의 특정 년월 데이터 조회
     */
    CommonResponseDTO<StoreSummaryResponse> getStoreSummary(Long sessionId, String summaryYearMonth);
    
    /**
     * 특정 회원의 최신 요약 데이터 조회
     */
    CommonResponseDTO<StoreSummaryResponse> getLatestStoreSummary(Long sessionId);
    
    /**
     * 매장 요약 데이터 목록 조회 (페이징)
     */
    CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryList(StoreSummaryListRequest request);
    
    /**
     * 특정 회원의 요약 이력 조회
     */
    CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryHistory(Long sessionId, Integer page, Integer limit);
    
    /**
     * 회원 요약 데이터 삭제
     */
    CommonResponseDTO<Void> deleteStoreSummary(Long sessionId, String summaryYearMonth);
    
    /**
     * 특정 회원의 모든 요약 데이터 삭제
     */
    CommonResponseDTO<Void> deleteAllStoreSummaryBySessionId(Long sessionId);
    
    /**
     * 사업주별 매장 요약 데이터 조회
     */
    CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryByOwnerId(Long ownerId, String summaryYearMonth, Integer page, Integer limit);
    
    /**
     * 현재 로그인한 사용자(session_id)의 매장 요약 데이터 조회
     */
    CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryBySessionId(String summaryYearMonth, Integer page, Integer limit);
    
    /**
     * CSV 데이터 업로드 및 store_summary 테이블 업데이트
     */
    CommonResponseDTO<StoreSummaryResponse> uploadCsvData(StoreSummaryCsvUploadRequest request);
}