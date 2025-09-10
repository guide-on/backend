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
        
        log.info("매장 요약 데이터 생성 요청: storeId={}, summaryYearMonth={}", request.getMemberId(), request.getSummaryYearMonth());
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.createStoreSummary(request);
        
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @PutMapping
    @ApiOperation(value = "매장 요약 데이터 수정", notes = "기존 매장 요약 데이터를 수정합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> updateStoreSummary(
            @RequestBody StoreSummaryUpdateRequest request) {
        
        log.info("매장 요약 데이터 수정 요청: storeId={}, summaryYearMonth={}",
                request.getMemberId(), request.getSummaryYearMonth());
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.updateStoreSummary(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{memberId}")
    @ApiOperation(value = "특정 년월 매장 요약 데이터 조회", notes = "특정 회원의 특정 년월 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> getStoreSummary(
            @ApiParam(value = "회원 ID", required = true) @PathVariable Long memberId,
            @ApiParam(value = "요약 년월 (YYYY-MM)", required = true) @RequestParam String summaryYearMonth) {
        
        log.info("매장 요약 데이터 조회 요청: memberId={}, summaryYearMonth={}", memberId, summaryYearMonth);
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.getStoreSummary(memberId, summaryYearMonth);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{memberId}/latest")
    @ApiOperation(value = "최신 매장 요약 데이터 조회", notes = "특정 회원의 최신 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<StoreSummaryResponse>> getLatestStoreSummary(
            @ApiParam(value = "회원 ID", required = true) @PathVariable Long memberId) {
        
        log.info("최신 매장 요약 데이터 조회 요청: memberId={}", memberId);
        
        CommonResponseDTO<StoreSummaryResponse> response = 
            storeSummaryService.getLatestStoreSummary(memberId);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping
    @ApiOperation(value = "매장 요약 데이터 목록 조회", notes = "조건에 따른 매장 요약 데이터 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getStoreSummaryList(
            @ApiParam(value = "회원 ID (선택)") @RequestParam(required = false) Long memberId,
            @ApiParam(value = "사업주 ID (선택)") @RequestParam(required = false) Long ownerId,
            @ApiParam(value = "사업자등록번호 (선택)") @RequestParam(required = false) String businessRegistrationNo,
            @ApiParam(value = "요약 년월 (선택)") @RequestParam(required = false) String summaryYearMonth,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("매장 요약 데이터 목록 조회 요청: memberId={}, ownerId={}, page={}, limit={}", 
                memberId, ownerId, page, limit);
        
        StoreSummaryListRequest request = new StoreSummaryListRequest();
        request.setMemberId(memberId);
        request.setOwnerId(ownerId);
        request.setBusinessRegistrationNo(businessRegistrationNo);
        request.setSummaryYearMonth(summaryYearMonth);
        request.setPage(page);
        request.setLimit(limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryList(request);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{memberId}/history")
    @ApiOperation(value = "매장 요약 이력 조회", notes = "특정 회원의 요약 이력을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getStoreSummaryHistory(
            @ApiParam(value = "회원 ID", required = true) @PathVariable Long memberId,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("매장 요약 이력 조회 요청: memberId={}, page={}, limit={}", memberId, page, limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryHistory(memberId, page, limit);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{memberId}")
    @ApiOperation(value = "매장 요약 데이터 삭제", notes = "특정 회원의 특정 년월 요약 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteStoreSummary(
            @ApiParam(value = "회원 ID", required = true) @PathVariable Long memberId,
            @ApiParam(value = "요약 년월 (YYYY-MM)", required = true) @RequestParam String summaryYearMonth) {
        
        log.info("매장 요약 데이터 삭제 요청: memberId={}, summaryYearMonth={}", memberId, summaryYearMonth);
        
        CommonResponseDTO<Void> response = 
            storeSummaryService.deleteStoreSummary(memberId, summaryYearMonth);
        
        return ResponseEntity.ok(response);
    }
    
    @DeleteMapping("/{memberId}/all")
    @ApiOperation(value = "회원 모든 요약 데이터 삭제", notes = "특정 회원의 모든 요약 데이터를 삭제합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> deleteAllStoreSummaryByMemberId(
            @ApiParam(value = "회원 ID", required = true) @PathVariable Long memberId) {
        
        log.info("회원 모든 요약 데이터 삭제 요청: memberId={}", memberId);
        
        CommonResponseDTO<Void> response = 
            storeSummaryService.deleteAllStoreSummaryByMemberId(memberId);
        
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
    
    @GetMapping("/my-data")
    @ApiOperation(value = "현재 로그인한 사용자의 매장 요약 데이터 조회", notes = "현재 로그인한 사용자의 member_id로 매장 요약 데이터를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<StoreSummaryResponse>>> getMyStoreSummary(
            @ApiParam(value = "요약 년월 (선택)") @RequestParam(required = false) String summaryYearMonth,
            @ApiParam(value = "페이지 번호", defaultValue = "1") @RequestParam(defaultValue = "1") Integer page,
            @ApiParam(value = "페이지당 개수", defaultValue = "20") @RequestParam(defaultValue = "20") Integer limit) {
        
        log.info("현재 로그인한 사용자의 매장 요약 데이터 조회 요청: summaryYearMonth={}, page={}, limit={}", 
                summaryYearMonth, page, limit);
        
        CommonResponseDTO<List<StoreSummaryResponse>> response = 
            storeSummaryService.getStoreSummaryByMemberId(summaryYearMonth, page, limit);
        
        return ResponseEntity.ok(response);
    }
}