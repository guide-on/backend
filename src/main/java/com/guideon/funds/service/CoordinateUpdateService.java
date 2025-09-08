package com.guideon.funds.service;

import com.guideon.funds.domain.SupportCenter;
import com.guideon.funds.dto.CoordinateUpdateResponseDTO;
import com.guideon.funds.dto.KakaoApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 좌표 업데이트 서비스
 */
@Service
@Transactional
public class CoordinateUpdateService {
    
    private static final Logger logger = LoggerFactory.getLogger(CoordinateUpdateService.class);
    
    @Autowired
    private SupportCenterService supportCenterService;
    
    @Autowired
    private KakaoApiService kakaoApiService;
    
    /**
     * 특정 지원센터의 좌표 업데이트
     * @param centerId 지원센터 ID
     * @return 업데이트 결과
     */
    public CoordinateUpdateResponseDTO updateCenterCoordinates(Long centerId) {
        logger.info("지원센터 ID {} 좌표 업데이트 시작", centerId);
        
        try {
            // 1. 지원센터 정보 조회
            SupportCenter center = supportCenterService.getCenterById(centerId);
            if (center == null) {
                return CoordinateUpdateResponseDTO.builder()
                        .centerId(centerId)
                        .updated(false)
                        .errorMessage("지원센터를 찾을 수 없습니다.")
                        .build();
            }
            
            // 2. 주소가 없으면 실패
            if (center.getAddress() == null || center.getAddress().trim().isEmpty()) {
                return CoordinateUpdateResponseDTO.builder()
                        .centerId(centerId)
                        .centerName(center.getName())
                        .updated(false)
                        .errorMessage("주소 정보가 없습니다.")
                        .build();
            }
            
            // 3. 카카오 API로 좌표 검색
            Double[] coordinates = kakaoApiService.getCoordinatesFromAddress(center.getAddress());
            
            if (coordinates == null) {
                return CoordinateUpdateResponseDTO.builder()
                        .centerId(centerId)
                        .centerName(center.getName())
                        .updated(false)
                        .errorMessage("주소에서 좌표를 찾을 수 없습니다.")
                        .build();
            }
            
            // 4. 좌표 업데이트
            center.setLat(coordinates[0]);  // 위도
            center.setLng(coordinates[1]);  // 경도
            
            boolean updateSuccess = supportCenterService.updateCenterCoordinates(center);
            
            if (updateSuccess) {
                logger.info("지원센터 ID {} 좌표 업데이트 성공", centerId);
                
                return CoordinateUpdateResponseDTO.builder()
                        .centerId(centerId)
                        .centerName(center.getName())
                        .foundAddress(center.getAddress())
                        .lat(coordinates[0])
                        .lng(coordinates[1])
                        .updated(true)
                        .build();
            } else {
                return CoordinateUpdateResponseDTO.builder()
                        .centerId(centerId)
                        .centerName(center.getName())
                        .updated(false)
                        .errorMessage("데이터베이스 업데이트에 실패했습니다.")
                        .build();
            }
            
        } catch (Exception e) {
            logger.error("지원센터 ID {} 좌표 업데이트 중 오류 발생", centerId, e);
            
            return CoordinateUpdateResponseDTO.builder()
                    .centerId(centerId)
                    .updated(false)
                    .errorMessage("처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }
    
    /**
     * 모든 지원센터의 좌표 일괄 업데이트
     * @return 업데이트 결과 목록
     */
    public List<CoordinateUpdateResponseDTO> updateAllCentersCoordinates() {
        logger.info("모든 지원센터 좌표 일괄 업데이트 시작");
        
        List<CoordinateUpdateResponseDTO> results = new ArrayList<>();
        
        try {
            // 1. 모든 지원센터 조회
            List<SupportCenter> allCenters = supportCenterService.getAllCenters();
            
            logger.info("총 {}개 지원센터의 좌표 업데이트 진행", allCenters.size());
            
            // 2. 각 센터별로 좌표 업데이트
            for (SupportCenter center : allCenters) {
                CoordinateUpdateResponseDTO result = updateCenterCoordinates(center.getId());
                results.add(result);
                
                // API 호출 제한을 고려한 딜레이 (선택사항)
                try {
                    Thread.sleep(100); // 0.1초 대기
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
            
            // 3. 결과 집계
            long successCount = results.stream().mapToLong(r -> r.isUpdated() ? 1 : 0).sum();
            long failCount = results.size() - successCount;
            
            logger.info("일괄 좌표 업데이트 완료 - 성공: {}개, 실패: {}개", successCount, failCount);
            
        } catch (Exception e) {
            logger.error("일괄 좌표 업데이트 중 오류 발생", e);
            
            // 오류 발생시 오류 정보 추가
            results.add(CoordinateUpdateResponseDTO.builder()
                    .updated(false)
                    .errorMessage("일괄 처리 중 오류 발생: " + e.getMessage())
                    .build());
        }
        
        return results;
    }
}
