package com.guideon.funds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 거리 계산 서비스 (Haversine 공식 사용)
 */
@Service
public class DistanceCalculationService {
    
    private static final Logger logger = LoggerFactory.getLogger(DistanceCalculationService.class);
    
    // 지구 반지름 (km)
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    /**
     * Haversine 공식을 사용한 두 좌표 간 거리 계산
     * @param lat1 첫 번째 지점의 위도
     * @param lng1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lng2 두 번째 지점의 경도
     * @return 거리 (km)
     */
    public double calculateDistanceKm(double lat1, double lng1, double lat2, double lng2) {
        logger.debug("거리 계산: ({}, {}) - ({}, {})", lat1, lng1, lat2, lng2);
        
        // 위도와 경도를 라디안으로 변환
        double lat1Rad = Math.toRadians(lat1);
        double lng1Rad = Math.toRadians(lng1);
        double lat2Rad = Math.toRadians(lat2);
        double lng2Rad = Math.toRadians(lng2);
        
        // 위도와 경도의 차이
        double deltaLat = lat2Rad - lat1Rad;
        double deltaLng = lng2Rad - lng1Rad;
        
        // Haversine 공식
        double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLng / 2) * Math.sin(deltaLng / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        double distance = EARTH_RADIUS_KM * c;
        
        logger.debug("계산된 거리: {} km", distance);
        
        return distance;
    }
    
    /**
     * 거리를 반올림하여 반환 (소수점 2자리)
     * @param lat1 첫 번째 지점의 위도
     * @param lng1 첫 번째 지점의 경도
     * @param lat2 두 번째 지점의 위도
     * @param lng2 두 번째 지점의 경도
     * @return 반올림된 거리 (km)
     */
    public double calculateDistanceKmRounded(double lat1, double lng1, double lat2, double lng2) {
        double distance = calculateDistanceKm(lat1, lng1, lat2, lng2);
        return Math.round(distance * 100.0) / 100.0; // 소수점 2자리 반올림
    }
}
