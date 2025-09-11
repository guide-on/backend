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

import java.math.BigDecimal;
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
            log.info("CSV 업로드 요청 처리 시작: {}", request);
            validateCsvUploadRequest(request);
            
            // 요청에서 받은 sessionId 사용
            Long actualSessionId = request.getSessionId();
            if (actualSessionId == null) {
                log.error("세션 ID가 null입니다.");
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            log.info("세션 ID 검증 완료: {}", actualSessionId);
            
            // CSV 데이터가 여러 행인 경우 첫 번째 행을 기준으로 업데이트
            // 실제로는 모든 데이터를 집계하거나 평균을 내는 로직이 필요할 수 있음
            log.info("CSV 데이터 행 수: {}", request.getSalesData().size());
            StoreSummaryCsvUploadRequest.SalesDataRow firstRow = request.getSalesData().get(0);
            log.info("첫 번째 CSV 행 데이터: {}", firstRow);
            
            // 요청된 사업자등록번호의 데이터 조회
            log.info("기존 데이터 조회 시작 - sessionId: {}, businessRegistrationNo: {}", actualSessionId, request.getBusinessRegistrationNo());
            StoreSummary existingStoreSummary = storeSummaryMapper.selectStoreSummary(actualSessionId, request.getBusinessRegistrationNo());
            log.info("기존 데이터 조회 결과: {}", existingStoreSummary != null ? "존재함" : "없음");
            
            StoreSummary storeSummary;
            if (existingStoreSummary == null) {
                // 해당 사업자등록번호 데이터가 없으면 기본 데이터를 생성
                log.info("기본 StoreSummary 객체 생성 중...");
                storeSummary = createDefaultStoreSummary(actualSessionId, request.getBusinessRegistrationNo());
                log.info("생성된 기본 StoreSummary: {}", storeSummary);
                
                // 새로운 기본 데이터 저장
                log.info("데이터베이스 INSERT 시작...");
                int insertResult = storeSummaryMapper.insertStoreSummary(storeSummary);
                log.info("INSERT 결과: {}", insertResult);
                if (insertResult == 0) {
                    throw new BadRequestException("기본 데이터 생성에 실패했습니다.");
                }
                log.info("세션 ID {} - {} 사업자등록번호에 대한 기본 매장 요약 데이터를 생성했습니다.", actualSessionId, request.getBusinessRegistrationNo());
            } else {
                // 기존 데이터 사용 (다른 필드들은 유지)
                log.info("기존 데이터 사용");
                storeSummary = existingStoreSummary;
            }
            
            // CSV 데이터로 매출 관련 필드만 업데이트
            log.info("CSV 데이터로 업데이트 시작...");
            updateStoreSummaryWithCsvData(storeSummary, firstRow);
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            log.info("업데이트된 StoreSummary: {}", storeSummary);
            
            // 기존 데이터의 sessionId와 businessRegistrationNo를 사용하여 업데이트
            log.info("데이터베이스 UPDATE 시작...");
            int updateResult = storeSummaryMapper.updateStoreSummary(storeSummary);
            log.info("UPDATE 결과: {}", updateResult);
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
    
    @Override
    @Transactional
    public void createDefaultStoreSummary(Long sessionId) {
        try {
            log.info("store_summary 기본값 데이터 생성 시작: sessionId={}", sessionId);
            
            // 이미 존재하는지 확인
            List<StoreSummary> existingData = storeSummaryMapper.selectStoreSummaryBySessionId(sessionId, 1, 0);
            if (!existingData.isEmpty()) {
                log.info("store_summary 데이터가 이미 존재합니다: sessionId={}", sessionId);
                return;
            }
            
            // 기본값으로 StoreSummary 객체 생성
            StoreSummary storeSummary = new StoreSummary();
            storeSummary.setSessionId(sessionId);
            storeSummary.setBusinessRegistrationNo("000-00-00000"); // 기본 사업자등록번호
            
            // 매출 관련 기본값 0으로 설정
            storeSummary.setTotalSalesAmount(new java.math.BigDecimal("0"));
            storeSummary.setWeekdaySalesAmount(new java.math.BigDecimal("0"));
            storeSummary.setWeekendSalesAmount(new java.math.BigDecimal("0"));
            storeSummary.setLunchSalesRatio(new java.math.BigDecimal("0"));
            storeSummary.setDinnerSalesRatio(new java.math.BigDecimal("0"));
            storeSummary.setTransactionCount(0);
            storeSummary.setWeekdayTransactionCount(0);
            storeSummary.setWeekendTransactionCount(0);
            storeSummary.setMomGrowthRate(new java.math.BigDecimal("0"));
            storeSummary.setYoyGrowthRate(new java.math.BigDecimal("0"));
            storeSummary.setSalesCv(new java.math.BigDecimal("0"));
            storeSummary.setAvgTransactionValue(new java.math.BigDecimal("0"));
            storeSummary.setCashPaymentRatio(new java.math.BigDecimal("0"));
            storeSummary.setCardPaymentRatio(new java.math.BigDecimal("0"));
            storeSummary.setRevisitCustomerSalesRatio(new java.math.BigDecimal("0"));
            storeSummary.setNewCustomerRatio(new java.math.BigDecimal("0"));
            
            // ESG 관련 기본값 0으로 설정
            storeSummary.setElectricityUsageKwh(new java.math.BigDecimal("0"));
            storeSummary.setElectricityBillAmount(new java.math.BigDecimal("0"));
            storeSummary.setGasUsageM3(new java.math.BigDecimal("0"));
            storeSummary.setWaterUsageTon(new java.math.BigDecimal("0"));
            storeSummary.setEnergyEffApplianceRatio(new java.math.BigDecimal("0"));
            storeSummary.setParticipateEnergyEffSupport(false);
            storeSummary.setParticipateHighEffEquipSupport(false);
            storeSummary.setFoodWasteKgPerDay(new java.math.BigDecimal("0"));
            storeSummary.setRecycleWasteKgPerDay(new java.math.BigDecimal("0"));
            storeSummary.setYellowUmbrellaMember(false);
            storeSummary.setYellowUmbrellaMonths(0);
            storeSummary.setYellowUmbrellaAmount(new java.math.BigDecimal("0"));
            storeSummary.setEmploymentInsuranceEmployees(0);
            storeSummary.setCustomerReviewAvgRating(new java.math.BigDecimal("0"));
            storeSummary.setCustomerReviewPositiveRatio(new java.math.BigDecimal("0"));
            storeSummary.setHygieneCertified(false);
            storeSummary.setOriginPriceViolationCount(0);
            
            // 재무 관련 기본값 0으로 설정
            storeSummary.setOperatingProfit(new java.math.BigDecimal("0"));
            storeSummary.setCostOfGoodsSold(new java.math.BigDecimal("0"));
            storeSummary.setTotalSalary(new java.math.BigDecimal("0"));
            storeSummary.setRentExpense(new java.math.BigDecimal("0"));
            storeSummary.setOtherExpenses(new java.math.BigDecimal("0"));
            storeSummary.setOperatingProfitRatio(new java.math.BigDecimal("0"));
            storeSummary.setCogsRatio(new java.math.BigDecimal("0"));
            storeSummary.setSalaryRatio(new java.math.BigDecimal("0"));
            storeSummary.setRentRatio(new java.math.BigDecimal("0"));
            
            // 현금흐름 관련 기본값 0으로 설정
            storeSummary.setCashPaymentRatioDetail(new java.math.BigDecimal("0"));
            storeSummary.setCardPaymentRatioDetail(new java.math.BigDecimal("0"));
            storeSummary.setOtherPaymentRatio(new java.math.BigDecimal("0"));
            storeSummary.setWeightedAvgCashPeriod(new java.math.BigDecimal("0"));
            storeSummary.setCashflowCv(new java.math.BigDecimal("0"));
            storeSummary.setAvgAccountBalance(new java.math.BigDecimal("0"));
            storeSummary.setMinBalanceMaintenanceRatio(new java.math.BigDecimal("0"));
            storeSummary.setExcessiveWithdrawalFrequency(new java.math.BigDecimal("0"));
            storeSummary.setRentPaymentComplianceRate(new java.math.BigDecimal("0"));
            storeSummary.setUtilityPaymentComplianceRate(new java.math.BigDecimal("0"));
            storeSummary.setSalaryPaymentRegularity(new java.math.BigDecimal("0"));
            storeSummary.setTaxPaymentIntegrity(new java.math.BigDecimal("0"));
            
            // 메타데이터 설정
            Timestamp now = new Timestamp(System.currentTimeMillis());
            storeSummary.setCreatedDttm(now);
            storeSummary.setUpdatedDttm(now);
            storeSummary.setLastUpdatedDttm(now);
            
            // 데이터베이스에 저장
            int result = storeSummaryMapper.insertStoreSummary(storeSummary);
            
            if (result != 1) {
                throw new BadRequestException("store_summary 테이블 데이터 생성에 실패했습니다.");
            }
            
            log.info("store_summary 기본값 데이터 생성 완료: sessionId={}", sessionId);
            
        } catch (Exception e) {
            log.error("store_summary 기본값 데이터 생성 중 오류 발생: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new BadRequestException("store_summary 기본값 데이터 생성 중 오류가 발생했습니다: " + e.getMessage());
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
        try {
            log.info("CSV 데이터 필드별 업데이트 시작");
            
            storeSummary.setTotalSalesAmount(salesData.getTotalSalesAmount());
            log.debug("totalSalesAmount 설정: {}", salesData.getTotalSalesAmount());
            
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
            
            log.info("CSV 데이터 필드별 업데이트 완료");
        } catch (Exception e) {
            log.error("CSV 데이터 업데이트 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("CSV 데이터 처리 중 오류가 발생했습니다: " + e.getMessage());
        }
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
        
        // 매출 관련 필드들을 0으로 초기화 (CSV에서 업데이트될 예정)
        storeSummary.setTotalSalesAmount(BigDecimal.ZERO);
        storeSummary.setWeekdaySalesAmount(BigDecimal.ZERO);
        storeSummary.setWeekendSalesAmount(BigDecimal.ZERO);
        storeSummary.setLunchSalesRatio(BigDecimal.ZERO);
        storeSummary.setDinnerSalesRatio(BigDecimal.ZERO);
        storeSummary.setTransactionCount(0);
        storeSummary.setWeekdayTransactionCount(0);
        storeSummary.setWeekendTransactionCount(0);
        storeSummary.setMomGrowthRate(BigDecimal.ZERO);
        storeSummary.setYoyGrowthRate(BigDecimal.ZERO);
        storeSummary.setSalesCv(BigDecimal.ZERO);
        storeSummary.setAvgTransactionValue(BigDecimal.ZERO);
        storeSummary.setCashPaymentRatio(BigDecimal.ZERO);
        storeSummary.setCardPaymentRatio(BigDecimal.ZERO);
        storeSummary.setRevisitCustomerSalesRatio(BigDecimal.ZERO);
        storeSummary.setNewCustomerRatio(BigDecimal.ZERO);
        
        // ESG 관련 필드들을 0 또는 false로 초기화
        storeSummary.setElectricityUsageKwh(BigDecimal.ZERO);
        storeSummary.setElectricityBillAmount(BigDecimal.ZERO);
        storeSummary.setGasUsageM3(BigDecimal.ZERO);
        storeSummary.setWaterUsageTon(BigDecimal.ZERO);
        storeSummary.setEnergyEffApplianceRatio(BigDecimal.ZERO);
        storeSummary.setParticipateEnergyEffSupport(false);
        storeSummary.setParticipateHighEffEquipSupport(false);
        storeSummary.setFoodWasteKgPerDay(BigDecimal.ZERO);
        storeSummary.setRecycleWasteKgPerDay(BigDecimal.ZERO);
        storeSummary.setYellowUmbrellaMember(false);
        storeSummary.setYellowUmbrellaMonths(0);
        storeSummary.setYellowUmbrellaAmount(BigDecimal.ZERO);
        storeSummary.setEmploymentInsuranceEmployees(0);
        storeSummary.setCustomerReviewAvgRating(BigDecimal.ZERO);
        storeSummary.setCustomerReviewPositiveRatio(BigDecimal.ZERO);
        storeSummary.setHygieneCertified(false);
        storeSummary.setOriginPriceViolationCount(0);
        
        // 재무 관련 필드들을 0으로 초기화
        storeSummary.setOperatingProfit(BigDecimal.ZERO);
        storeSummary.setCostOfGoodsSold(BigDecimal.ZERO);
        storeSummary.setTotalSalary(BigDecimal.ZERO);
        storeSummary.setRentExpense(BigDecimal.ZERO);
        storeSummary.setOtherExpenses(BigDecimal.ZERO);
        storeSummary.setOperatingProfitRatio(BigDecimal.ZERO);
        storeSummary.setCogsRatio(BigDecimal.ZERO);
        storeSummary.setSalaryRatio(BigDecimal.ZERO);
        storeSummary.setRentRatio(BigDecimal.ZERO);
        
        // 현금흐름 관련 필드들을 0으로 초기화
        storeSummary.setCashPaymentRatioDetail(BigDecimal.ZERO);
        storeSummary.setCardPaymentRatioDetail(BigDecimal.ZERO);
        storeSummary.setOtherPaymentRatio(BigDecimal.ZERO);
        storeSummary.setWeightedAvgCashPeriod(BigDecimal.ZERO);
        storeSummary.setCashflowCv(BigDecimal.ZERO);
        storeSummary.setAvgAccountBalance(BigDecimal.ZERO);
        storeSummary.setMinBalanceMaintenanceRatio(BigDecimal.ZERO);
        storeSummary.setExcessiveWithdrawalFrequency(BigDecimal.ZERO);
        storeSummary.setRentPaymentComplianceRate(BigDecimal.ZERO);
        storeSummary.setUtilityPaymentComplianceRate(BigDecimal.ZERO);
        storeSummary.setSalaryPaymentRegularity(BigDecimal.ZERO);
        storeSummary.setTaxPaymentIntegrity(BigDecimal.ZERO);
        
        return storeSummary;
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> updateCashflowData(Long sessionId) {
        try {
            log.info("현금흐름 데이터 업데이트 시작: sessionId={}", sessionId);
            
            // 세션 ID 검증
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            // 기존 데이터 조회
            List<StoreSummary> existingData = storeSummaryMapper.selectStoreSummaryBySessionId(sessionId, 1, 0);
            if (existingData.isEmpty()) {
                throw new BadRequestException("업데이트를 실패하였습니다. 해당 세션 ID에 대한 매장 요약 데이터가 존재하지 않습니다.");
            }
            
            StoreSummary storeSummary = existingData.get(0);
            
            // 현금흐름 관련 하드코딩 데이터로 업데이트
            storeSummary.setOperatingProfit(new BigDecimal("5000000.00"));              // 월별 영업이익 (원)
            storeSummary.setCostOfGoodsSold(new BigDecimal("8000000.00"));             // 월별 매출원가 (원)
            storeSummary.setTotalSalary(new BigDecimal("4000000.00"));                 // 월별 급여총액 (원)
            storeSummary.setRentExpense(new BigDecimal("2000000.00"));                 // 월별 임차료 (원)
            storeSummary.setOtherExpenses(new BigDecimal("1000000.00"));               // 월별 기타비용 (원)
            storeSummary.setOperatingProfitRatio(new BigDecimal("25.00"));             // 영업이익률 (%)
            storeSummary.setCogsRatio(new BigDecimal("40.00"));                        // 매출원가율 (%)
            storeSummary.setSalaryRatio(new BigDecimal("20.00"));                      // 급여비율 (%)
            storeSummary.setRentRatio(new BigDecimal("10.00"));                        // 임차료율 (%)
            storeSummary.setCashPaymentRatioDetail(new BigDecimal("15.50"));           // 현금 결제 비율 (상세) (%)
            storeSummary.setCardPaymentRatioDetail(new BigDecimal("80.00"));           // 카드 결제 비율 (상세) (%)
            storeSummary.setOtherPaymentRatio(new BigDecimal("4.50"));                 // 기타 결제 비율 (%)
            storeSummary.setWeightedAvgCashPeriod(new BigDecimal("2.50"));             // 가중평균 현금화 기간 (일)
            storeSummary.setCashflowCv(new BigDecimal("0.35"));                        // 현금흐름 변동계수 (CV)
            storeSummary.setAvgAccountBalance(new BigDecimal("15000000.00"));          // 평균 계좌 잔액 (원)
            storeSummary.setMinBalanceMaintenanceRatio(new BigDecimal("85.00"));       // 최소 잔액 유지 비율 (%)
            storeSummary.setExcessiveWithdrawalFrequency(new BigDecimal("2.00"));      // 과다 인출 빈도 (월별 횟수)
            storeSummary.setRentPaymentComplianceRate(new BigDecimal("100.00"));       // 임대료 납부 준수율 (%)
            storeSummary.setUtilityPaymentComplianceRate(new BigDecimal("95.00"));     // 공과금 납부 준수율 (%)
            storeSummary.setSalaryPaymentRegularity(new BigDecimal("100.00"));         // 급여 지급 정상성 (%)
            storeSummary.setTaxPaymentIntegrity(new BigDecimal("90.00"));              // 세금 납부 성실도 (%)
            
            // 업데이트 시간 설정
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            // 데이터베이스 업데이트 (전체 필드 업데이트)
            log.info("현금흐름 데이터 데이터베이스 업데이트 시작...");
            int updateResult = storeSummaryMapper.updateStoreSummaryFull(storeSummary);
            log.info("현금흐름 데이터 UPDATE 결과: {}", updateResult);
            
            if (updateResult == 0) {
                throw new BadRequestException("현금흐름 데이터 업데이트에 실패했습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            log.info("현금흐름 데이터 업데이트 완료: sessionId={}", sessionId);
            return CommonResponseDTO.success("현금흐름 건전성 관련 데이터가 성공적으로 업데이트되었습니다.", response);
            
        } catch (Exception e) {
            log.error("현금흐름 데이터 업데이트 중 오류 발생: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new BadRequestException("현금흐름 데이터 업데이트 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> updateEsgData(Long sessionId, Double energyEffRatio) {
        try {
            log.info("ESG 데이터 업데이트 시작: sessionId={}, energyEffRatio={}", sessionId, energyEffRatio);
            
            // 세션 ID 검증
            if (sessionId == null) {
                throw new BadRequestException("세션 ID는 필수 값입니다.");
            }
            
            if (energyEffRatio == null) {
                throw new BadRequestException("에너지 효율 기기 비율은 필수 값입니다.");
            }
            
            // 기존 데이터 조회
            List<StoreSummary> existingData = storeSummaryMapper.selectStoreSummaryBySessionId(sessionId, 1, 0);
            if (existingData.isEmpty()) {
                throw new BadRequestException("업데이트를 실패하였습니다. 해당 세션 ID에 대한 매장 요약 데이터가 존재하지 않습니다.");
            }
            
            StoreSummary storeSummary = existingData.get(0);
            
            // ESG 관련 데이터 업데이트
            storeSummary.setEnergyEffApplianceRatio(new BigDecimal(energyEffRatio.toString()));  // 에너지 효율 기기 비율
            storeSummary.setParticipateEnergyEffSupport(true);                                    // 에너지효율향상 지원사업 참여 여부 (1)
            storeSummary.setParticipateHighEffEquipSupport(true);                                 // 고효율기기 구매 지원사업 참여 여부 (1)
            
            // 업데이트 시간 설정
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            // 데이터베이스 업데이트 (전체 필드 업데이트)
            log.info("ESG 데이터 데이터베이스 업데이트 시작...");
            int updateResult = storeSummaryMapper.updateStoreSummaryFull(storeSummary);
            log.info("ESG 데이터 UPDATE 결과: {}", updateResult);
            
            if (updateResult == 0) {
                throw new BadRequestException("ESG 데이터 업데이트에 실패했습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            log.info("ESG 데이터 업데이트 완료: sessionId={}", sessionId);
            return CommonResponseDTO.success("ESG 관련 데이터가 성공적으로 업데이트되었습니다.", response);
            
        } catch (Exception e) {
            log.error("ESG 데이터 업데이트 중 오류 발생: sessionId={}, error={}", sessionId, e.getMessage(), e);
            throw new BadRequestException("ESG 데이터 업데이트 중 오류가 발생했습니다: " + e.getMessage());
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
