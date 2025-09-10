package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.BadRequestException;
import com.guideon.common.exception.NotFoundException;
import com.guideon.hybridEvaluation.domain.StoreSummary;
import com.guideon.hybridEvaluation.dto.StoreSummaryCsvUploadRequest;
import com.guideon.hybridEvaluation.dto.StoreSummaryResponse;
import com.guideon.hybridEvaluation.mapper.StoreSummaryMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StoreSummaryServiceImpl implements StoreSummaryService {
    
    private final StoreSummaryMapper storeSummaryMapper;

    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryBySessionId(Long sessionId, String summaryYearMonth, Integer page, Integer limit) {
        try {
            // 세션 ID 검증
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            if (page == null || page < 1) page = 1;
            if (limit == null || limit < 1) limit = 20;
            
            int offset = (page - 1) * limit;
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryBySessionId(
                sessionId, limit, offset);
            
            List<StoreSummaryResponse> responseList = storeSummaryList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success("현재 사용자의 매장 요약 데이터를 성공적으로 조회했습니다.", responseList);
            
        } catch (Exception e) {
            log.error("현재 사용자의 매장 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("현재 사용자의 매장 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> uploadCsvData(StoreSummaryCsvUploadRequest request) {
        try {
            validateCsvUploadRequest(request);
            
            // 요청에서 받은 sessionId 사용
            Long actualSessionId = request.getSessionId();
            if (actualSessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            // CSV 데이터가 여러 행인 경우 첫 번째 행을 기준으로 업데이트
            // 실제로는 모든 데이터를 집계하거나 평균을 내는 로직이 필요할 수 있음
            StoreSummaryCsvUploadRequest.SalesDataRow firstRow = request.getSalesData().get(0);
            
            // 요청된 사업자등록번호의 데이터 조회
            StoreSummary existingStoreSummary = storeSummaryMapper.selectStoreSummary(actualSessionId, request.getBusinessRegistrationNo());
            
            StoreSummary storeSummary;
            if (existingStoreSummary == null) {
                // 해당 사업자등록번호 데이터가 없으면 기본 데이터를 생성
                storeSummary = createDefaultStoreSummary(actualSessionId, request.getBusinessRegistrationNo());
                
                // 새로운 기본 데이터 저장
                int insertResult = storeSummaryMapper.insertStoreSummary(storeSummary);
                if (insertResult == 0) {
                    throw new BadRequestException("기본 데이터 생성에 실패했습니다.");
                }
                log.info("세션 ID {} - {} 사업자등록번호에 대한 기본 매장 요약 데이터를 생성했습니다.", actualSessionId, request.getBusinessRegistrationNo());
            } else {
                // 기존 데이터 사용 (다른 필드들은 유지)
                storeSummary = existingStoreSummary;
            }
            
            // CSV 데이터로 매출 관련 필드만 업데이트
            updateStoreSummaryWithCsvData(storeSummary, firstRow);
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            // 기존 데이터의 sessionId와 businessRegistrationNo를 사용하여 업데이트
            int updateResult = storeSummaryMapper.updateStoreSummary(storeSummary);
            if (updateResult == 0) {
                throw new BadRequestException("데이터 업데이트에 실패했습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            return CommonResponseDTO.success("CSV 데이터가 성공적으로 업로드되었습니다.", response);
            
        } catch (Exception e) {
            log.error("CSV 데이터 업로드 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("CSV 데이터 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    private void validateCsvUploadRequest(StoreSummaryCsvUploadRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getSessionId() == null) {
            throw new BadRequestException("세션 ID는 필수 입력값입니다.");
        }
        
        if (request.getBusinessRegistrationNo() == null || request.getBusinessRegistrationNo().trim().isEmpty()) {
            throw new BadRequestException("사업자등록번호는 필수 입력값입니다.");
        }
        
        if (request.getSalesData() == null || request.getSalesData().isEmpty()) {
            throw new BadRequestException("매출 데이터가 비어있습니다.");
        }
    }
    
    private void updateStoreSummaryWithCsvData(StoreSummary storeSummary, StoreSummaryCsvUploadRequest.SalesDataRow salesData) {
        storeSummary.setTotalSalesAmount(salesData.getTotalSalesAmount());
        storeSummary.setWeekdaySalesAmount(salesData.getWeekdaySalesAmount());
        storeSummary.setWeekendSalesAmount(salesData.getWeekendSalesAmount());
        storeSummary.setLunchSalesRatio(salesData.getLunchSalesRatio());
        storeSummary.setDinnerSalesRatio(salesData.getDinnerSalesRatio());
        storeSummary.setTransactionCount(salesData.getTransactionCount());
        storeSummary.setWeekdayTransactionCount(salesData.getWeekdayTransactionCount());
        storeSummary.setWeekendTransactionCount(salesData.getWeekendTransactionCount());
        storeSummary.setMomGrowthRate(salesData.getMomGrowthRate());
        storeSummary.setYoyGrowthRate(salesData.getYoyGrowthRate());
        storeSummary.setSalesCv(salesData.getSalesCv());
        storeSummary.setAvgTransactionValue(salesData.getAvgTransactionValue());
        storeSummary.setCashPaymentRatio(salesData.getCashPaymentRatio());
        storeSummary.setCardPaymentRatio(salesData.getCardPaymentRatio());
        storeSummary.setRevisitCustomerSalesRatio(salesData.getRevisitCustomerSalesRatio());
        storeSummary.setNewCustomerRatio(salesData.getNewCustomerRatio());
    }
    
    /**
     * 기본 매장 요약 데이터를 생성합니다.
     */
    private StoreSummary createDefaultStoreSummary(Long sessionId, String businessRegistrationNo) {
        StoreSummary storeSummary = new StoreSummary();
        
        // 필수 정보 설정
        storeSummary.setSessionId(sessionId);
        storeSummary.setBusinessRegistrationNo(businessRegistrationNo);
        
        // 타임스탬프 설정
        Timestamp now = new Timestamp(System.currentTimeMillis());
        storeSummary.setCreatedDttm(now);
        storeSummary.setUpdatedDttm(now);
        storeSummary.setLastUpdatedDttm(now);
        
        // 매출 관련 필드들은 null로 초기화 (CSV에서 업데이트될 예정)
        // ESG, 재무, 현금흐름 관련 필드들도 null로 초기화
        
        return storeSummary;
    }
    
    private StoreSummaryResponse convertToResponse(StoreSummary storeSummary) {
        if (storeSummary == null) {
            return null;
        }
        
        StoreSummaryResponse response = new StoreSummaryResponse();
        BeanUtils.copyProperties(storeSummary, response);
        return response;
    }
}
