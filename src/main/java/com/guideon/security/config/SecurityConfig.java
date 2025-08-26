package com.guideon.security.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
@Slf4j
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    public void configure(HttpSecurity http) throws Exception {
        // 기본 설정으로 시작 - 모든 요청에 인증 필요
        http.authorizeRequests() //  요청 권한 설정
                .anyRequest().authenticated() // 모든 요청에 인증 필요
                .and()
                .formLogin() // 기본 로그인 폼 활성화
                .and()
                .logout(); // 로그아웃 기능 활성화
    }
}
