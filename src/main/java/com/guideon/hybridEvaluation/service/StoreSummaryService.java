package com.guideon.hybridEvaluation.service;

import com.guideon.hybridEvaluation.dto.*;
import com.guideon.common.dto.CommonResponseDTO;

import java.util.List;

public interface StoreSummaryService {

    /**
     * 세션 ID로 매장 요약 데이터 조회
     */
    CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryBySessionId(Long sessionId, String summaryYearMonth, Integer page, Integer limit);

    /**
     * CSV 데이터 업로드 및 store_summary 테이블 업데이트
     */
    CommonResponseDTO<StoreSummaryResponse> uploadCsvData(StoreSummaryCsvUploadRequest request);
    
    /**
     * store_summary 테이블에 기본값 데이터 생성
     */
    void createDefaultStoreSummary(Long sessionId);

    /**
     * 현금흐름 건전성 관련 데이터 업데이트
     */
    CommonResponseDTO<StoreSummaryResponse> updateCashflowData(Long sessionId);
}