package com.guideon.funds.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideon.funds.dto.KakaoApiResponseDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Collections;

/**
 * 카카오 Local API 서비스
 */
@Service
public class KakaoApiService {
    
    private static final Logger logger = LoggerFactory.getLogger(KakaoApiService.class);
    
    private static final String KAKAO_API_URL = "https://dapi.kakao.com/v2/local/search/address.json";
    
    @Value("${kakao.api.key:}")
    private String kakaoApiKey;
    
    @Autowired
    private AddressPreprocessService addressPreprocessService;
    
    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    
    public KakaoApiService() {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
    }
    
    /**
     * 카카오 Local API를 통해 주소의 좌표를 검색
     * @param address 검색할 주소
     * @return 카카오 API 응답 결과
     */
    public KakaoApiResponseDTO searchAddressCoordinates(String address) {
        logger.info("=== 카카오 API 주소 검색 시작 ===");
        logger.info("검색 주소: {}", address);
        
        try {
            // API 키 확인
            if (kakaoApiKey == null || kakaoApiKey.trim().isEmpty()) {
                logger.error("카카오 API 키가 설정되지 않았습니다.");
                return null;
            }
            
            logger.info("카카오 API 키 확인 완료: {}****", kakaoApiKey.substring(0, Math.min(8, kakaoApiKey.length())));
            
            // URL 생성
            String url = UriComponentsBuilder.fromHttpUrl(KAKAO_API_URL)
                    .queryParam("query", address)
                    .queryParam("analyze_type", "similar")  // 유사한 주소 포함 검색
                    .build()
                    .toUriString();
            
            // HTTP 헤더 설정
            HttpHeaders headers = new HttpHeaders();
            headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
            headers.set("Authorization", "KakaoAK " + kakaoApiKey);
            
            HttpEntity<String> entity = new HttpEntity<>(headers);
            
            // API 호출
            logger.info("카카오 API 호출 URL: {}", url);
            logger.info("요청 헤더: {}", headers);
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url, 
                    HttpMethod.GET, 
                    entity, 
                    String.class
            );
            
            logger.info("카카오 API 응답 상태: {}", response.getStatusCode());
            logger.info("카카오 API 응답 내용: {}", response.getBody());
            
            // 응답 파싱
            if (response.getStatusCode() == HttpStatus.OK) {
                KakaoApiResponseDTO result = objectMapper.readValue(
                    response.getBody(), 
                    KakaoApiResponseDTO.class
                );
                
                logger.info("파싱 결과 - 검색 결과: {}개", 
                           result.getDocuments() != null ? result.getDocuments().size() : 0);
                
                if (result.getDocuments() != null && !result.getDocuments().isEmpty()) {
                    KakaoApiResponseDTO.Document firstResult = result.getDocuments().get(0);
                    logger.info("첫 번째 결과: address={}, x={}, y={}", 
                               firstResult.getAddress_name(), firstResult.getX(), firstResult.getY());
                } else {
                    logger.warn("검색 결과가 없습니다.");
                }
                
                return result;
            } else {
                logger.error("카카오 API 호출 실패 - Status: {}, Body: {}", 
                           response.getStatusCode(), response.getBody());
                return null;
            }
            
        } catch (Exception e) {
            logger.error("카카오 API 호출 중 오류 발생: {}", e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * 첫 번째 검색 결과에서 좌표 추출
     * @param address 검색할 주소
     * @return [위도, 경도] 배열, 실패시 null
     */
    public Double[] getCoordinatesFromAddress(String address) {
        logger.info("좌표 추출 시작 - 원본 주소: {}", address);
        
        // 여러 형태의 주소로 시도
        String[] addressVariations = addressPreprocessService.getAddressVariations(address);
        
        for (int i = 0; i < addressVariations.length; i++) {
            String tryAddress = addressVariations[i];
            logger.info("주소 변형 {}번째 시도: {}", i + 1, tryAddress);
            
            KakaoApiResponseDTO result = searchAddressCoordinates(tryAddress);
            
            if (result != null && 
                result.getDocuments() != null && 
                !result.getDocuments().isEmpty()) {
                
                KakaoApiResponseDTO.Document firstResult = result.getDocuments().get(0);
                
                try {
                    double lat = Double.parseDouble(firstResult.getY());  // Y = 위도
                    double lng = Double.parseDouble(firstResult.getX());  // X = 경도
                    
                    logger.info("좌표 추출 성공 - 사용된 주소: {}, 위도: {}, 경도: {}", tryAddress, lat, lng);
                    
                    return new Double[]{lat, lng};
                    
                } catch (NumberFormatException e) {
                    logger.error("좌표 변환 실패 - X: {}, Y: {}", firstResult.getX(), firstResult.getY());
                }
            }
        }
        
        logger.warn("모든 주소 변형으로 검색 실패: {}", address);
        return null;
    }
}
