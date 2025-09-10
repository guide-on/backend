package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.BadRequestException;
import com.guideon.common.exception.NotFoundException;
import com.guideon.hybridEvaluation.domain.StoreSummary;
import com.guideon.hybridEvaluation.dto.*;
import com.guideon.hybridEvaluation.mapper.StoreSummaryMapper;
import com.guideon.security.util.LoginUserProvider;
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
    private final LoginUserProvider loginUserProvider;
    
    @Override
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> createStoreSummary(StoreSummaryCreateRequest request) {
        try {
            // 로그인한 사용자의 sessionId 가져오기 및 검증
            Long loginSessionId = loginUserProvider.getLoginSessionId();
            if (loginSessionId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            validateCreateRequest(request);
            
            StoreSummary storeSummary = new StoreSummary();
            BeanUtils.copyProperties(request, storeSummary);
            storeSummary.setCreatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            int result = storeSummaryMapper.insertStoreSummary(storeSummary);
            
            if (result > 0) {
                StoreSummary savedData = storeSummaryMapper.selectStoreSummary(
                    storeSummary.getSessionId(),
                    storeSummary.getSummaryYearMonth()
                );
                
                StoreSummaryResponse response = convertToResponse(savedData);
                
                return CommonResponseDTO.success("매장 요약 데이터가 성공적으로 생성되었습니다.", response);
            } else {
                throw new BadRequestException("매장 요약 데이터 생성에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("매장 요약 데이터 생성 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> updateStoreSummary(StoreSummaryUpdateRequest request) {
        try {
            // 로그인한 사용자의 sessionId 가져오기 및 검증
            Long loginSessionId = loginUserProvider.getLoginSessionId();
            if (loginSessionId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            validateUpdateRequest(request);
            
            boolean exists = storeSummaryMapper.existsStoreSummary(
                request.getSessionId(), 
                request.getSummaryYearMonth()
            );
            
            if (!exists) {
                throw new NotFoundException("수정할 매장 요약 데이터를 찾을 수 없습니다.");
            }
            
            StoreSummary storeSummary = new StoreSummary();
            BeanUtils.copyProperties(request, storeSummary);
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            int result = storeSummaryMapper.updateStoreSummary(storeSummary);
            
            if (result > 0) {
                StoreSummary updatedData = storeSummaryMapper.selectStoreSummary(
                    storeSummary.getSessionId(),
                    storeSummary.getSummaryYearMonth()
                );
                
                StoreSummaryResponse response = convertToResponse(updatedData);
                
                return CommonResponseDTO.success("매장 요약 데이터가 성공적으로 수정되었습니다.", response);
            } else {
                throw new BadRequestException("매장 요약 데이터 수정에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("매장 요약 데이터 수정 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 데이터 수정 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<StoreSummaryResponse> getStoreSummary(Long sessionId, String summaryYearMonth) {
        try {
            if (sessionId == null || summaryYearMonth == null || summaryYearMonth.trim().isEmpty()) {
                throw new BadRequestException("세션 ID와 요약 년월은 필수 입력값입니다.");
            }
            
            StoreSummary storeSummary = storeSummaryMapper.selectStoreSummary(sessionId, summaryYearMonth);
            
            if (storeSummary == null) {
                throw new NotFoundException("해당 회원의 요약 데이터를 찾을 수 없습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            return CommonResponseDTO.success("회원 요약 데이터를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("회원 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("회원 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<StoreSummaryResponse> getLatestStoreSummary(Long sessionId) {
        try {
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 입력값입니다.");
            }
            
            StoreSummary storeSummary = storeSummaryMapper.selectLatestStoreSummary(sessionId);
            
            if (storeSummary == null) {
                throw new NotFoundException("해당 회원의 요약 데이터를 찾을 수 없습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            return CommonResponseDTO.success("최신 회원 요약 데이터를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("최신 회원 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("최신 회원 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryList(StoreSummaryListRequest request) {
        try {
            validateListRequest(request);
            
            int offset = (request.getPage() - 1) * request.getLimit();
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryList(
                request.getSessionId(),
                request.getOwnerId(),
                request.getBusinessRegistrationNo(),
                request.getSummaryYearMonth(),
                request.getLimit(),
                offset
            );
            
            List<StoreSummaryResponse> responseList = storeSummaryList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success("매장 요약 데이터 목록을 성공적으로 조회했습니다.", responseList);
            
        } catch (Exception e) {
            log.error("매장 요약 데이터 목록 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 데이터 목록 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryHistory(Long sessionId, Integer page, Integer limit) {
        try {
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 입력값입니다.");
            }
            
            if (page == null || page < 1) page = 1;
            if (limit == null || limit < 1) limit = 20;
            
            int offset = (page - 1) * limit;
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryHistory(sessionId, limit, offset);
            
            List<StoreSummaryResponse> responseList = storeSummaryList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success("회원 요약 이력을 성공적으로 조회했습니다.", responseList);
            
        } catch (Exception e) {
            log.error("회원 요약 이력 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("회원 요약 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteStoreSummary(Long sessionId, String summaryYearMonth) {
        try {
            if (sessionId == null || summaryYearMonth == null || summaryYearMonth.trim().isEmpty()) {
                throw new BadRequestException("세션 ID와 요약 년월은 필수 입력값입니다.");
            }
            
            boolean exists = storeSummaryMapper.existsStoreSummary(sessionId, summaryYearMonth);
            
            if (!exists) {
                throw new NotFoundException("삭제할 회원 요약 데이터를 찾을 수 없습니다.");
            }
            
            int result = storeSummaryMapper.deleteStoreSummary(sessionId, summaryYearMonth);
            
            if (result > 0) {
                return CommonResponseDTO.success("회원 요약 데이터가 성공적으로 삭제되었습니다.", null);
            } else {
                throw new BadRequestException("회원 요약 데이터 삭제에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("회원 요약 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("회원 요약 데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteAllStoreSummaryBySessionId(Long sessionId) {
        try {
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 입력값입니다.");
            }
            
            int result = storeSummaryMapper.deleteAllStoreSummaryBySessionId(sessionId);
            
            return CommonResponseDTO.success(
                String.format("회원(ID: %d)의 모든 요약 데이터 %d건이 삭제되었습니다.", sessionId, result), null);
            
        } catch (Exception e) {
            log.error("회원의 모든 요약 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("회원의 모든 요약 데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryByOwnerId(Long ownerId, String summaryYearMonth, Integer page, Integer limit) {
        try {
            if (ownerId == null) {
                throw new BadRequestException("사업주 ID는 필수 입력값입니다.");
            }
            
            if (page == null || page < 1) page = 1;
            if (limit == null || limit < 1) limit = 20;
            
            int offset = (page - 1) * limit;
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryByOwnerId(
                ownerId, summaryYearMonth, limit, offset);
            
            List<StoreSummaryResponse> responseList = storeSummaryList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success("사업주별 매장 요약 데이터를 성공적으로 조회했습니다.", responseList);
            
        } catch (Exception e) {
            log.error("사업주별 매장 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("사업주별 매장 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryByMemberId(String summaryYearMonth, Integer page, Integer limit) {
        try {
            // 로그인한 사용자의 sessionId 가져오기
            Long loginSessionId = loginUserProvider.getLoginSessionId();
            if (loginSessionId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            if (page == null || page < 1) page = 1;
            if (limit == null || limit < 1) limit = 20;
            
            int offset = (page - 1) * limit;
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryByMemberId(
                loginSessionId, summaryYearMonth, limit, offset);
            
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
            // 로그인한 사용자의 sessionId 가져오기 및 검증
            Long loginSessionId = loginUserProvider.getLoginSessionId();
            if (loginSessionId == null) {
                throw new BadRequestException("로그인이 필요합니다.");
            }
            
            validateCsvUploadRequest(request);
            
            // 로그인한 사용자의 sessionId를 사용 (요청의 sessionId 대신)
            Long actualSessionId = loginSessionId;
            
            // CSV 데이터가 여러 행인 경우 첫 번째 행을 기준으로 업데이트
            // 실제로는 모든 데이터를 집계하거나 평균을 내는 로직이 필요할 수 있음
            StoreSummaryCsvUploadRequest.SalesDataRow firstRow = request.getSalesData().get(0);
            
            // 요청된 년월의 데이터 조회
            StoreSummary existingStoreSummary = storeSummaryMapper.selectStoreSummary(actualSessionId, request.getSummaryYearMonth());
            
            StoreSummary storeSummary;
            if (existingStoreSummary == null) {
                // 해당 년월 데이터가 없으면 기본 데이터를 생성
                storeSummary = createDefaultStoreSummary(actualSessionId, request.getSummaryYearMonth());
                
                // 새로운 기본 데이터 저장
                int insertResult = storeSummaryMapper.insertStoreSummary(storeSummary);
                if (insertResult == 0) {
                    throw new BadRequestException("기본 데이터 생성에 실패했습니다.");
                }
                log.info("세션 ID {} - {}년월에 대한 기본 매장 요약 데이터를 생성했습니다.", actualSessionId, request.getSummaryYearMonth());
            } else {
                // 기존 데이터 사용 (다른 필드들은 유지)
                storeSummary = existingStoreSummary;
            }
            
            // CSV 데이터로 매출 관련 필드만 업데이트
            updateStoreSummaryWithCsvData(storeSummary, firstRow);
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            // 기존 데이터의 sessionId와 summaryYearMonth를 사용하여 업데이트
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
        
        if (request.getSummaryYearMonth() == null || request.getSummaryYearMonth().trim().isEmpty()) {
            throw new BadRequestException("요약 년월은 필수 입력값입니다.");
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
        storeSummary.setWeekdayAvgTransactionValue(salesData.getWeekdayAvgTransactionValue());
        storeSummary.setWeekendAvgTransactionValue(salesData.getWeekendAvgTransactionValue());
        storeSummary.setCashPaymentRatio(salesData.getCashPaymentRatio());
        storeSummary.setCardPaymentRatio(salesData.getCardPaymentRatio());
        storeSummary.setRevisitCustomerSalesRatio(salesData.getRevisitCustomerSalesRatio());
        storeSummary.setNewCustomerRatio(salesData.getNewCustomerRatio());
    }
    
    /**
     * 기본 매장 요약 데이터를 생성합니다.
     */
    private StoreSummary createDefaultStoreSummary(Long sessionId, String summaryYearMonth) {
        StoreSummary storeSummary = new StoreSummary();
        
        // 필수 정보 설정
        storeSummary.setMemberId(sessionId);
        storeSummary.setOwnerId(sessionId); // 기본적으로 sessionId와 동일하게 설정
        storeSummary.setBusinessRegistrationNo("DEFAULT"); // 기본 사업자등록번호
        storeSummary.setCurrentMonth(1); // 기본 영업개월수
        storeSummary.setSummaryYearMonth(summaryYearMonth);
        
        // 타임스탬프 설정
        Timestamp now = new Timestamp(System.currentTimeMillis());
        storeSummary.setCreatedDttm(now);
        storeSummary.setUpdatedDttm(now);
        storeSummary.setLastUpdatedDttm(now);
        
        // 매출 관련 필드들은 null로 초기화 (CSV에서 업데이트될 예정)
        // ESG, 재무, 현금흐름 관련 필드들도 null로 초기화
        
        return storeSummary;
    }
    
    private void validateCreateRequest(StoreSummaryCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getSessionId() == null) {
            throw new BadRequestException("세션 ID는 필수 입력값입니다.");
        }
        
        if (request.getOwnerId() == null) {
            throw new BadRequestException("사업주 ID는 필수 입력값입니다.");
        }
        
        if (request.getBusinessRegistrationNo() == null || request.getBusinessRegistrationNo().trim().isEmpty()) {
            throw new BadRequestException("사업자등록번호는 필수 입력값입니다.");
        }
        
        if (request.getSummaryYearMonth() == null || request.getSummaryYearMonth().trim().isEmpty()) {
            throw new BadRequestException("요약 년월은 필수 입력값입니다.");
        }
        
        boolean exists = storeSummaryMapper.existsStoreSummary(request.getSessionId(), request.getSummaryYearMonth());
        if (exists) {
            throw new BadRequestException("이미 존재하는 회원 요약 데이터입니다.");
        }
    }
    
    private void validateUpdateRequest(StoreSummaryUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getSessionId() == null) {
            throw new BadRequestException("세션 ID는 필수 입력값입니다.");
        }
        
        if (request.getSummaryYearMonth() == null || request.getSummaryYearMonth().trim().isEmpty()) {
            throw new BadRequestException("요약 년월은 필수 입력값입니다.");
        }
    }
    
    private void validateListRequest(StoreSummaryListRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getPage() == null || request.getPage() < 1) {
            request.setPage(1);
        }
        
        if (request.getLimit() == null || request.getLimit() < 1) {
            request.setLimit(20);
        }
        
        if (request.getLimit() > 100) {
            request.setLimit(100);
        }
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