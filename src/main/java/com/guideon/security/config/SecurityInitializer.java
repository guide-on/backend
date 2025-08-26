package com.guideon.security.config;

import org.springframework.security.web.context.AbstractSecurityWebApplicationInitializer;

/**
 * Spring Security 필터 체인을 등록하는 초기화 클래스
 * 웹 애플리케이션 시작 시 자동으로 Security 필터들을 등록
 */
public class SecurityInitializer extends AbstractSecurityWebApplicationInitializer {
    // 별도 구현 불필요 - 상위 클래스에서 자동 처리
}