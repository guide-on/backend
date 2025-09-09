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
    
    @PostMapping
    @ApiOperation(value = "매장 요약 데이터 생성", notes = "새로운 매장 요약 데이터를 생성합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> createStoreSummary(
            @RequestBody StoreSummaryCreateRequest request) {
        
        log.info("매장 요약 데이터 생성 요청: storeId={}, summaryYearMonth={}", request.getStoreId(), request.getSummaryYearMonth());
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.createStoreSummary(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    @ApiOperation(value = "매장 요약 데이터 수정", notes = "기존 매장 요약 데이터를 수정합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> updateStoreSummary(
            @RequestBody StoreSummaryUpdateRequest request) {
        
        log.info("매장 요약 데이터 수정 요청: storeId={}, summaryYearMonth={}", 
                request.getStoreId(), request.getSummaryYearMonth());
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.updateStoreSummary(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{storeId}")
    @ApiOperation(value = "특정 년월 매장 요약 데이터 조회", notes = "특정 매장의 특정 년월 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> getStoreSummary(
            @ApiParam(value = "매장 ID", required = true) @PathVariable Long storeId,
            @ApiParam(value = "요약 년월 (YYYY-MM)", required = true) @RequestParam String summaryYearMonth) {
        
        log.info("매장 요약 데이터 조회 요청: storeId={}, summaryYearMonth={}", storeId, summaryYearMonth);
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.getStoreSummary(storeId, summaryYearMonth);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{storeId}/latest")
    @ApiOperation(value = "최신 매장 요약 데이터 조회", notes = "특정 매장의 최신 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> getLatestStoreSummary(
            @ApiParam(value = "매장 ID", required = true) @PathVariable Long storeId) {
        
        log.info("최신 매장 요약 데이터 조회 요청: storeId={}", storeId);
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.getLatestStoreSummary(storeId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @ApiOperation(value = "매장 요약 데이터 목록 조회", notes = "조건에 따른 매장 요약 데이터 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getStoreSummaryList(
            @ApiParam(value = "매장 ID (선택)") @RequestParam(required = false) Long storeId,
            @ApiParam(value = "사업주 ID (선택)") @RequestParam(required = false) Long ownerId,
            @ApiParam(value = "사업자등록번호 (선택)") @RequestParam(required = false) String businessRegistrationNo,
            @ApiParam(value = "요약 년월 (선택)") @RequestParam(required = false) String summaryYearMonth,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("매장 요약 데이터 목록 조회 요청: storeId={}, ownerId={}, page={}, limit={}", 
                storeId, ownerId, page, limit);
        
        StoreSummaryListRequest request = new StoreSummaryListRequest();
        request.setStoreId(storeId);
        request.setOwnerId(ownerId);
        request.setBusinessRegistrationNo(businessRegistrationNo);
        request.setSummaryYearMonth(summaryYearMonth);
        request.setPage(page);
        request.setLimit(limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryList(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{storeId}/history")
    @ApiOperation(value = "매장 요약 이력 조회", notes = "특정 매장의 요약 이력을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getStoreSummaryHistory(
            @ApiParam(value = "매장 ID", required = true) @PathVariable Long storeId,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("매장 요약 이력 조회 요청: storeId={}, page={}, limit={}", storeId, page, limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryHistory(storeId, page, limit);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{storeId}")
    @ApiOperation(value = "매장 요약 데이터 삭제", notes = "특정 매장의 특정 년월 요약 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteStoreSummary(
            @ApiParam(value = "매장 ID", required = true) @PathVariable Long storeId,
            @ApiParam(value = "요약 년월 (YYYY-MM)", required = true) @RequestParam String summaryYearMonth) {
        
        log.info("매장 요약 데이터 삭제 요청: storeId={}, summaryYearMonth={}", storeId, summaryYearMonth);
        
        CommonResponseDTO<Void> response = 
            storeSummaryService.deleteStoreSummary(storeId, summaryYearMonth);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{storeId}/all")
    @ApiOperation(value = "매장 모든 요약 데이터 삭제", notes = "특정 매장의 모든 요약 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAllStoreSummaryByStoreId(
            @ApiParam(value = "매장 ID", required = true) @PathVariable Long storeId) {
        
        log.info("매장 모든 요약 데이터 삭제 요청: storeId={}", storeId);
        
        CommonResponseDTO<Void> response = 
            storeSummaryService.deleteAllStoreSummaryByStoreId(storeId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/owner/{ownerId}")
    @ApiOperation(value = "사업주별 매장 요약 데이터 조회", notes = "특정 사업주의 매장 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getStoreSummaryByOwnerId(
            @ApiParam(value = "사업주 ID", required = true) @PathVariable Long ownerId,
            @ApiParam(value = "요약 년월 (선택)") @RequestParam(required = false) String summaryYearMonth,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("사업주별 매장 요약 데이터 조회 요청: ownerId={}, summaryYearMonth={}, page={}, limit={}", 
                ownerId, summaryYearMonth, page, limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryByOwnerId(ownerId, summaryYearMonth, page, limit);
        
        return ResponseEntity.ok(response);
    }
}