package com.guideon.hybridEvaluation.service;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.BadRequestException;
import com.guideon.common.exception.NotFoundException;
import com.guideon.hybridEvaluation.domain.StoreSummary;
import com.guideon.hybridEvaluation.dto.*;
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
    @Transactional
    public CommonResponseDTO<StoreSummaryResponse> createStoreSummary(StoreSummaryCreateRequest request) {
        try {
            validateCreateRequest(request);
            
            StoreSummary storeSummary = new StoreSummary();
            BeanUtils.copyProperties(request, storeSummary);
            storeSummary.setCreatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            storeSummary.setLastUpdatedDttm(new Timestamp(System.currentTimeMillis()));
            
            int result = storeSummaryMapper.insertStoreSummary(storeSummary);
            
            if (result > 0) {
                StoreSummary savedData = storeSummaryMapper.selectStoreSummary(
                    storeSummary.getStoreId(), 
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
            validateUpdateRequest(request);
            
            boolean exists = storeSummaryMapper.existsStoreSummary(
                request.getStoreId(), 
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
                    storeSummary.getStoreId(), 
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
    public CommonResponseDTO<StoreSummaryResponse> getStoreSummary(Long storeId, String summaryYearMonth) {
        try {
            if (storeId == null || summaryYearMonth == null || summaryYearMonth.trim().isEmpty()) {
                throw new BadRequestException("매장 ID와 요약 년월은 필수 입력값입니다.");
            }
            
            StoreSummary storeSummary = storeSummaryMapper.selectStoreSummary(storeId, summaryYearMonth);
            
            if (storeSummary == null) {
                throw new NotFoundException("해당 매장 요약 데이터를 찾을 수 없습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            return CommonResponseDTO.success("매장 요약 데이터를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("매장 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<StoreSummaryResponse> getLatestStoreSummary(Long storeId) {
        try {
            if (storeId == null) {
                throw new BadRequestException("매장 ID는 필수 입력값입니다.");
            }
            
            StoreSummary storeSummary = storeSummaryMapper.selectLatestStoreSummary(storeId);
            
            if (storeSummary == null) {
                throw new NotFoundException("해당 매장의 요약 데이터를 찾을 수 없습니다.");
            }
            
            StoreSummaryResponse response = convertToResponse(storeSummary);
            
            return CommonResponseDTO.success("최신 매장 요약 데이터를 성공적으로 조회했습니다.", response);
            
        } catch (Exception e) {
            log.error("최신 매장 요약 데이터 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("최신 매장 요약 데이터 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryList(StoreSummaryListRequest request) {
        try {
            validateListRequest(request);
            
            int offset = (request.getPage() - 1) * request.getLimit();
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryList(
                request.getStoreId(),
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
    public CommonResponseDTO<List<StoreSummaryResponse>> getStoreSummaryHistory(Long storeId, Integer page, Integer limit) {
        try {
            if (storeId == null) {
                throw new BadRequestException("매장 ID는 필수 입력값입니다.");
            }
            
            if (page == null || page < 1) page = 1;
            if (limit == null || limit < 1) limit = 20;
            
            int offset = (page - 1) * limit;
            
            List<StoreSummary> storeSummaryList = storeSummaryMapper.selectStoreSummaryHistory(storeId, limit, offset);
            
            List<StoreSummaryResponse> responseList = storeSummaryList.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
            
            return CommonResponseDTO.success("매장 요약 이력을 성공적으로 조회했습니다.", responseList);
            
        } catch (Exception e) {
            log.error("매장 요약 이력 조회 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 이력 조회 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteStoreSummary(Long storeId, String summaryYearMonth) {
        try {
            if (storeId == null || summaryYearMonth == null || summaryYearMonth.trim().isEmpty()) {
                throw new BadRequestException("매장 ID와 요약 년월은 필수 입력값입니다.");
            }
            
            boolean exists = storeSummaryMapper.existsStoreSummary(storeId, summaryYearMonth);
            
            if (!exists) {
                throw new NotFoundException("삭제할 매장 요약 데이터를 찾을 수 없습니다.");
            }
            
            int result = storeSummaryMapper.deleteStoreSummary(storeId, summaryYearMonth);
            
            if (result > 0) {
                return CommonResponseDTO.success("매장 요약 데이터가 성공적으로 삭제되었습니다.", null);
            } else {
                throw new BadRequestException("매장 요약 데이터 삭제에 실패했습니다.");
            }
            
        } catch (Exception e) {
            log.error("매장 요약 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장 요약 데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional
    public CommonResponseDTO<Void> deleteAllStoreSummaryByStoreId(Long storeId) {
        try {
            if (storeId == null) {
                throw new BadRequestException("매장 ID는 필수 입력값입니다.");
            }
            
            int result = storeSummaryMapper.deleteAllStoreSummaryByStoreId(storeId);
            
            return CommonResponseDTO.success(
                String.format("매장(ID: %d)의 모든 요약 데이터 %d건이 삭제되었습니다.", storeId, result), null);
            
        } catch (Exception e) {
            log.error("매장의 모든 요약 데이터 삭제 중 오류 발생: {}", e.getMessage(), e);
            throw new BadRequestException("매장의 모든 요약 데이터 삭제 중 오류가 발생했습니다: " + e.getMessage());
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
    
    private void validateCreateRequest(StoreSummaryCreateRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getStoreId() == null) {
            throw new BadRequestException("매장 ID는 필수 입력값입니다.");
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
        
        boolean exists = storeSummaryMapper.existsStoreSummary(request.getStoreId(), request.getSummaryYearMonth());
        if (exists) {
            throw new BadRequestException("이미 존재하는 매장 요약 데이터입니다.");
        }
    }
    
    private void validateUpdateRequest(StoreSummaryUpdateRequest request) {
        if (request == null) {
            throw new BadRequestException("요청 데이터가 비어있습니다.");
        }
        
        if (request.getStoreId() == null) {
            throw new BadRequestException("매장 ID는 필수 입력값입니다.");
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