package com.guideon.funds.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.regex.Pattern;

/**
 * 주소 전처리 서비스
 */
@Service
public class AddressPreprocessService {
    
    private static final Logger logger = LoggerFactory.getLogger(AddressPreprocessService.class);
    
    /**
     * 카카오 API 검색에 적합하도록 주소를 전처리
     * @param originalAddress 원본 주소
     * @return 전처리된 주소
     */
    public String preprocessAddress(String originalAddress) {
        if (originalAddress == null || originalAddress.trim().isEmpty()) {
            return originalAddress;
        }
        
        logger.info("주소 전처리 시작: {}", originalAddress);
        
        String processed = originalAddress;
        
        // 1. 우편번호 제거 - (12345) 형태
        processed = processed.replaceAll("\\(\\d{5}\\)", "").trim();
        
        // 2. 불필요한 문구 제거
        processed = processed.replaceAll("\\s+\\d+층.*", ""); // X층 이후 모든 내용 제거
        processed = processed.replaceAll("\\s*,.*", ""); // 쉼표 이후 모든 내용 제거
        
        // 3. 건물명 중복 제거 및 정리
        // "건물명 건물명" 같은 중복 제거
        String[] parts = processed.split("\\s+");
        StringBuilder result = new StringBuilder();
        
        for (int i = 0; i < parts.length; i++) {
            String part = parts[i].trim();
            
            // 빈 문자열 스킵
            if (part.isEmpty()) continue;
            
            // 층수 관련 단어 스킵
            if (part.matches("\\d+층?") || part.equals("층")) continue;
            
            // 중복 단어 제거 (연속된 같은 단어)
            if (i > 0 && part.equals(parts[i-1])) continue;
            
            if (result.length() > 0) {
                result.append(" ");
            }
            result.append(part);
        }
        
        processed = result.toString().trim();
        
        // 4. 기본 주소만 추출 (시/군/구 + 도로명/지번 + 번지)
        // 건물명이 있어도 기본 주소까지만 사용
        String[] addressParts = processed.split("\\s+");
        StringBuilder basicAddress = new StringBuilder();
        
        for (String part : addressParts) {
            basicAddress.append(part).append(" ");
            
            // 번지가 나오면 거기서 중단 (건물명 제외)
            if (part.matches(".*\\d+(-\\d+)?$") && basicAddress.toString().split("\\s+").length >= 3) {
                break;
            }
        }
        
        processed = basicAddress.toString().trim();
        
        logger.info("주소 전처리 완료: {} -> {}", originalAddress, processed);
        
        return processed;
    }
    
    /**
     * 여러 가지 주소 형태로 시도
     * @param originalAddress 원본 주소
     * @return 전처리된 주소 배열 (우선순위 순)
     */
    public String[] getAddressVariations(String originalAddress) {
        String basic = preprocessAddress(originalAddress);
        
        // 기본 전처리된 주소
        String variation1 = basic;
        
        // 더 간단하게 - 도/시/군/구 + 도로명/동명만
        String[] parts = basic.split("\\s+");
        StringBuilder simple = new StringBuilder();
        for (int i = 0; i < Math.min(parts.length, 3); i++) {
            if (i > 0) simple.append(" ");
            simple.append(parts[i]);
        }
        String variation2 = simple.toString();
        
        // 건물명까지 포함
        String variation3 = originalAddress.replaceAll("\\(\\d{5}\\)", "").replaceAll("\\s+\\d+층.*", "").trim();
        
        return new String[]{variation1, variation2, variation3};
    }
}
