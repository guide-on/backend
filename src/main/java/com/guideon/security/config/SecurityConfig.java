package com.guideon.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.web.cors.CorsConfiguration;                  // CORS
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    /**
     * 로컬 개발 편의 모드:
     * - /api/community/** 전체 오픈
     * - CSRF 비활성화 (개발 중 POST/PUT/DELETE 테스트 편의)
     * 운영 전에는 반드시 false!
     */
    @Value("${guideon.security.dev-relaxed:false}")
    private boolean devRelaxed;

    // 공용 공개 리소스(permitAll)
    private static final String[] PUBLIC_PATHS = new String[] {
            "/", "/error", "/favicon.ico",
            // 정적/문서
            "/resources/**", "/webjars/**",
            "/swagger-ui.html", "/swagger-resources/**", "/v2/api-docs",
            "/css/**", "/js/**", "/images/**", "/index.html"
            // 로그인/로그아웃은 아래 formLogin()/logout()에서 permitAll 처리
    };

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.cors(); // 필요 시

        if (devRelaxed) {
            log.warn("▶▶ [DEV RELAXED MODE] 활성화: /api/community/** permitAll, CSRF disabled");
            http.csrf().disable();

            http.authorizeRequests()
                    .antMatchers(PUBLIC_PATHS).permitAll()
                    .antMatchers("/api/community/**").permitAll()   // 개발 중 커뮤니티 API 오픈
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()     // 기본 로그인 페이지 사용 (loginPage 미지정)
                    .permitAll()
                    .and()
                    .logout()
                    .logoutUrl("/logout")
                    .permitAll();

            // H2 콘솔 같은 걸 쓸 경우(프레임 허용):
            // http.headers().frameOptions().sameOrigin();

        } else {
            // 운영 기본 정책: CSRF 기본 활성(명시 호출 불필요)
            http.authorizeRequests()
                    .antMatchers(PUBLIC_PATHS).permitAll()
                    .anyRequest().authenticated()
                    .and()
                    .formLogin()
                    .permitAll() // 기본 로그인 페이지
                    .and()
                    .logout()
                    .logoutUrl("/logout")
                    .permitAll();
        }
    }

    /** CORS (Vite dev 등에서 필요 시) */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowCredentials(true);
        c.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://127.0.0.1:5173"
        ));
        c.setAllowedMethods(Arrays.asList("GET","POST","PUT","DELETE","PATCH","OPTIONS"));
        c.setAllowedHeaders(Arrays.asList(
                "Authorization","Cache-Control","Content-Type","X-Requested-With","X-CSRF-TOKEN"
        ));
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
