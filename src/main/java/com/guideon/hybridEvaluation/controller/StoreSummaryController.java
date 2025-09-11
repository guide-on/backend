package com.guideon.hybridEvaluation.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.hybridEvaluation.dto.*;
import com.guideon.hybridEvaluation.service.StoreSummaryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/store-summary")
@RequiredArgsConstructor
@Api(tags = "매장 요약 데이터 관리")
public class StoreSummaryController {
    
    private final StoreSummaryService storeSummaryService;
    
    @GetMapping("/my-data/{sessionId}")
    @ApiOperation(value = "세션 ID로 매장 요약 데이터 조회", notes = "특정 세션 ID로 매장 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getMyStoreSummary(
            @ApiParam(value = "세션 ID", required = true) @PathVariable Long sessionId,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {

        log.info("세션 ID로 매장 요약 데이터 조회 요청: sessionId={}, page={}, limit={}",
                sessionId, page, limit);

        CommonResponseDTO<List<StoreSummaryResponse>> response =
            storeSummaryService.getStoreSummaryBySessionId(sessionId, null, page, limit);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/upload-csv")
    @ApiOperation(value = "CSV 데이터 업로드", notes = "매출 관련 CSV 데이터를 업로드하여 store_summary 테이블을 업데이트합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> uploadCsvData(
            @RequestBody StoreSummaryCsvUploadRequest request) {

        log.info("CSV 데이터 업로드 요청: sessionId={}, businessRegistrationNo={}, dataCount={}",
                request.getSessionId(), request.getBusinessRegistrationNo(),
                request.getSalesData() != null ? request.getSalesData().size() : 0);

        CommonResponseDTO<StoreSummaryResponse> response =
            storeSummaryService.uploadCsvData(request);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-cashflow/{sessionId}")
    @ApiOperation(value = "현금흐름 데이터 업데이트", notes = "계좌 연결 후 현금흐름 건전성 관련 데이터를 업데이트합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> updateCashflowData(
            @ApiParam(value = "세션 ID", required = true) @PathVariable Long sessionId) {

        log.info("현금흐름 데이터 업데이트 요청: sessionId={}", sessionId);

        CommonResponseDTO<StoreSummaryResponse> response =
            storeSummaryService.updateCashflowData(sessionId);

        return ResponseEntity.ok(response);
    }

    @PutMapping("/update-esg/{sessionId}")
    @ApiOperation(value = "ESG 데이터 업데이트", notes = "ESG 관련 데이터를 업데이트합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> updateEsgData(
            @ApiParam(value = "세션 ID", required = true) @PathVariable Long sessionId,
            @ApiParam(value = "에너지 효율 기기 비율", required = true) @RequestParam Double energyEffRatio) {

        log.info("ESG 데이터 업데이트 요청: sessionId={}, energyEffRatio={}", sessionId, energyEffRatio);

        CommonResponseDTO<StoreSummaryResponse> response =
            storeSummaryService.updateEsgData(sessionId, energyEffRatio);

        return ResponseEntity.ok(response);
    }
}