package com.guideon.funds.service;

import com.guideon.funds.domain.SupportCenter;
import com.guideon.funds.dto.NearestCenterRequestDTO;
import com.guideon.funds.dto.NearestCenterResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 가장 가까운 센터 찾기 서비스
 */
@Service
public class NearestCenterService {
    
    private static final Logger logger = LoggerFactory.getLogger(NearestCenterService.class);
    
    @Autowired
    private SupportCenterService supportCenterService;
    
    @Autowired
    private KakaoApiService kakaoApiService;
    
    @Autowired
    private DistanceCalculationService distanceCalculationService;
    
    /**
     * 사업장 주소 기반 가장 가까운 센터 찾기
     * @param request 요청 정보
     * @return 가까운 센터 목록
     */
    public NearestCenterResponseDTO findNearestCenters(NearestCenterRequestDTO request) {
        logger.info("가까운 센터 찾기 시작 - 주소: {}, 제한: {}개", 
                   request.getBusinessAddress(), request.getLimit());
        
        try {
            // 1. 사업장 주소의 좌표 조회
            Double[] businessCoordinates = kakaoApiService.getCoordinatesFromAddress(request.getBusinessAddress());
            
            if (businessCoordinates == null) {
                return NearestCenterResponseDTO.builder()
                        .searchedAddress(request.getBusinessAddress())
                        .success(false)
                        .errorMessage("사업장 주소에서 좌표를 찾을 수 없습니다.")
                        .build();
            }
            
            double businessLat = businessCoordinates[0];
            double businessLng = businessCoordinates[1];
            
            logger.info("사업장 좌표: 위도={}, 경도={}", businessLat, businessLng);
            
            // 2. 좌표가 있는 모든 센터 조회
            List<SupportCenter> centersWithCoordinates = supportCenterService.getCentersWithCoordinates();
            
            if (centersWithCoordinates.isEmpty()) {
                return NearestCenterResponseDTO.builder()
                        .searchedAddress(request.getBusinessAddress())
                        .businessLat(businessLat)
                        .businessLng(businessLng)
                        .success(false)
                        .errorMessage("좌표가 설정된 지원센터가 없습니다.")
                        .build();
            }
            
            logger.info("좌표가 있는 센터 {}개 발견", centersWithCoordinates.size());
            
            // 3. 각 센터와의 거리 계산
            List<NearestCenterResponseDTO.NearestCenter> nearestCenters = new ArrayList<>();
            
            for (SupportCenter center : centersWithCoordinates) {
                double distance = distanceCalculationService.calculateDistanceKmRounded(
                    businessLat, businessLng, 
                    center.getLat(), center.getLng()
                );
                
                nearestCenters.add(
                    NearestCenterResponseDTO.NearestCenter.builder()
                        .center(center)
                        .distanceKm(distance)
                        .build()
                );
                
                logger.debug("센터: {}, 거리: {} km", center.getName(), distance);
            }
            
            // 4. 거리순 정렬
            nearestCenters.sort(Comparator.comparing(NearestCenterResponseDTO.NearestCenter::getDistanceKm));
            
            // 5. 제한된 개수만 반환 (기본값: 1개)
            int limit = request.getLimit() != null ? request.getLimit() : 1;
            List<NearestCenterResponseDTO.NearestCenter> limitedCenters = nearestCenters.stream()
                    .limit(limit)
                    .collect(Collectors.toList());
            
            // 6. 순위 설정
            for (int i = 0; i < limitedCenters.size(); i++) {
                limitedCenters.get(i).setRank(i + 1);
            }
            
            logger.info("가까운 센터 찾기 완료 - 결과: {}개", limitedCenters.size());
            
            if (!limitedCenters.isEmpty()) {
                NearestCenterResponseDTO.NearestCenter closest = limitedCenters.get(0);
                logger.info("가장 가까운 센터: {} ({}km)", 
                           closest.getCenter().getName(), closest.getDistanceKm());
            }
            
            return NearestCenterResponseDTO.builder()
                    .searchedAddress(request.getBusinessAddress())
                    .businessLat(businessLat)
                    .businessLng(businessLng)
                    .nearestCenters(limitedCenters)
                    .success(true)
                    .build();
            
        } catch (Exception e) {
            logger.error("가까운 센터 찾기 중 오류 발생", e);
            
            return NearestCenterResponseDTO.builder()
                    .searchedAddress(request.getBusinessAddress())
                    .success(false)
                    .errorMessage("처리 중 오류가 발생했습니다: " + e.getMessage())
                    .build();
        }
    }
}
