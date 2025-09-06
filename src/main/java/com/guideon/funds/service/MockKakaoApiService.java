package com.guideon.funds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * 카카오 API 목 서비스 (테스트용)
 */
@Service
@Profile("test") // 테스트 프로파일에서만 활성화
public class MockKakaoApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(MockKakaoApiService.class);
    
    /**
     * 목 좌표 데이터 반환 (테스트용)
     */
    public Double[] getCoordinatesFromAddress(String address) {
        logger.info("목 서비스 - 주소: {}", address);
        
        // 지역별 대략적인 좌표 반환 (테스트용)
        if (address.contains("강릉")) {
            return new Double[]{37.7519, 128.8761}; // 강릉시청 근처
        } else if (address.contains("춘천")) {
            return new Double[]{37.8813, 127.7299}; // 춘천시청 근처
        } else if (address.contains("의정부")) {
            return new Double[]{37.7380, 127.0334}; // 의정부시청 근처
        } else if (address.contains("창원")) {
            return new Double[]{35.2280, 128.6811}; // 창원시청 근처
        } else if (address.contains("경주")) {
            return new Double[]{35.8562, 129.2247}; // 경주시청 근처
        } else {
            // 기본값: 서울시청
            return new Double[]{37.5666, 126.9779};
        }
    }
}
