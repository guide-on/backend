package com.guideon.security.config;

import com.guideon.security.filter.AuthenticationErrorFilter;
import com.guideon.security.filter.JwtAuthenticationFilter;
import com.guideon.security.filter.JwtUsernamePasswordAuthenticationFilter;
import com.guideon.security.handler.CustomAccessDeniedHandler;
import com.guideon.security.handler.CustomAuthenticationEntryPoint;
import com.guideon.security.policy.AccessPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.configurers.ExpressionUrlAuthorizationConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableWebSecurity
@Slf4j
@ComponentScan(basePackages = {"com.guideon.security"})
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {
    private final UserDetailsService userDetailsService;
    // JWT 인증 필터
    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    // 인증 예외처리 필터
    private final AuthenticationErrorFilter authenticationErrorFilter;

    // 401/403 에러 처리 핸들러
    private final CustomAuthenticationEntryPoint authenticationEntryPoint;
    private final CustomAccessDeniedHandler accessDeniedHandler;

    @Autowired
    private JwtUsernamePasswordAuthenticationFilter jwtUsernamePasswordAuthenticationFilter;

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // UserDetailsService와 PasswordEncoder 설정
        auth.userDetailsService(userDetailsService)  // 커스텀 서비스 사용
                .passwordEncoder(passwordEncoder()); // BCrypt 암호화 사용
    }

    /**
     * CORS 설정 - 모든 도메인 허용
     */
    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.setAllowCredentials(true);               // 인증 정보 포함 허용
        config.addAllowedOriginPattern("*");            // 모든 도메인 허용
        config.addAllowedHeader("*");                   // 모든 헤더 허용
        config.addAllowedMethod("*");                   // 모든 HTTP 메서드 허용

        source.registerCorsConfiguration("/**", config);
        return new CorsFilter(source);
    }

    // 문자셋 필터 메서드
    public CharacterEncodingFilter encodingFilter() {
        CharacterEncodingFilter encodingFilter = new CharacterEncodingFilter();
        encodingFilter.setEncoding("UTF-8");           // UTF-8 인코딩 설정
        encodingFilter.setForceEncoding(true);         // 강제 인코딩 적용
        return encodingFilter;
    }

    @Override
    public void configure(HttpSecurity http) throws Exception {
        http.addFilterBefore(encodingFilter(), CsrfFilter.class)
                .addFilterBefore(authenticationErrorFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtUsernamePasswordAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        //예외 처리 설정
        http.exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint)
                .accessDeniedHandler(accessDeniedHandler);

        //  HTTP 보안 설정
        http.httpBasic().disable()      // 기본 HTTP 인증 비활성화
                .csrf().disable()           // CSRF 보호 비활성화 (REST API에서는 불필요)
                .formLogin().disable()      // 폼 로그인 비활성화 (JSON 기반 API 사용)
                .sessionManagement()        // 세션 관리 설정
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS);  // 무상태 모드

        http.cors();

        ExpressionUrlAuthorizationConfigurer<HttpSecurity>.ExpressionInterceptUrlRegistry registry = http.authorizeRequests();
        // permitAll 경로 등록
        for (AccessPolicy.AccessRule rule : AccessPolicy.PERMIT_ALL) {
            if (rule.method != null) {
                registry.antMatchers(rule.method, rule.uriPattern).permitAll();
            } else {
                registry.antMatchers(rule.uriPattern).permitAll();
            }
        }
        registry.anyRequest().authenticated(); // 나머지 인증 필요
    }
}
