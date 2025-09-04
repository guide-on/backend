package com.guideon.security.policy;

import org.springframework.http.HttpMethod;

import java.util.List;

public class AccessPolicy {

    // permitAll 리스트
    public static final List<AccessRule> PERMIT_ALL = List.of(
            // Swagger UI 관련 경로 추가 (가장 위에 배치)
            new AccessRule(null, "/swagger-ui.html", null),
            new AccessRule(null, "/swagger-ui/**", null),
            new AccessRule(null, "/swagger-resources/**", null),
            new AccessRule(null, "/v2/api-docs", null),
            new AccessRule(null, "/v2/api-docs/**", null),
            new AccessRule(null, "/webjars/**", null),
            new AccessRule(null, "/webjars/springfox-swagger-ui/**", null),

            // (권장) CORS preflight 허용
            new AccessRule(HttpMethod.OPTIONS, "/**", null),

            // 회원 관련
            new AccessRule(HttpMethod.POST, "/api/member", null), // 회원가입
            new AccessRule(HttpMethod.GET, "/api/member/exist/email/**", null), // 이메일 중복 체크
            new AccessRule(HttpMethod.POST, "/api/verification/**", null), // 이메일/전화번호 인증
            new AccessRule(HttpMethod.POST, "/api/member/find/**", null), // 아이디 찾기
            new AccessRule(HttpMethod.POST, "/api/member/password/reset", null), // 비밀번호 재설정

            // 인증 토큰
            new AccessRule(HttpMethod.POST, "/api/auth/reissue", null), // 토큰 재발급
            new AccessRule(HttpMethod.POST, "/api/auth/logout", null),   // 로그아웃

            // ── 커뮤니티: 비로그인 허용(홈/리스트/검색/인기) ───────────
            // 리스트
            new AccessRule(HttpMethod.GET, "/api/community/posts", null), // 리스트 조회
            new AccessRule(HttpMethod.GET, "/api/community/posts/search", null), // 검색
            new AccessRule(HttpMethod.GET, "/api/community/posts/popular", null) // 인기
    );

    // 인증 필요 (명시적으로 지정 필요 시 사용)
//    public static final List<AccessRule> AUTHENTICATED = List.of(
//            new AccessRule(HttpMethod.POST, "/api/member/update", "AUTHENTICATED")
//    );

    // 역할 구분 필요시 사용 (참고용)
//    public static final List<AccessRule> ADMIN_ONLY = List.of(
//            new AccessRule(HttpMethod.GET, "/api/admin/**", "ROLE_ADMIN")
//    );

    public static class AccessRule {
        public final HttpMethod method;
        public final String uriPattern;
        public final String role; // null: permitAll, "AUTHENTICATED", "ROLE_ADMIN"

        public AccessRule(HttpMethod method, String uriPattern, String role) {
            this.method = method;
            this.uriPattern = uriPattern;
            this.role = role;
        }
    }
}
