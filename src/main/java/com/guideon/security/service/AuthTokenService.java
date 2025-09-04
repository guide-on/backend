package com.guideon.security.service;

import com.guideon.common.redis.RedisKeyUtil;
import com.guideon.common.redis.RedisService;
import com.guideon.security.account.domain.MemberVO;
import com.guideon.security.util.CookieUtil;
import com.guideon.security.util.JwtConstants;
import com.guideon.security.util.JwtProcessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletResponse;

@Service
@RequiredArgsConstructor
public class AuthTokenService {
    public static final int REFRESH_TOKEN_EXP_MINUTES = JwtConstants.REFRESH_TOKEN_EXP_SECONDS / 60;

    private final JwtProcessor jwtProcessor;
    private final RedisService redisService;

    // 토큰 발급 후 HttpOnly 쿠키에 저장
    public void issueTokenAndSetCookie(HttpServletResponse response, MemberVO member) {
        String email = member.getEmail();
        Long memberId = member.getMemberId();

        // JWT 토큰 생성
        String access = jwtProcessor.generateAccessToken(memberId, email);
        String refresh = jwtProcessor.generateRefreshToken(memberId);

        // Redis에 저장 (key = "RT:<memberId>", value = token)
        redisService.set(RedisKeyUtil.refreshToken(memberId), refresh,  REFRESH_TOKEN_EXP_MINUTES);

        CookieUtil.addCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, access, JwtConstants.ACCESS_TOKEN_EXP_SECONDS, "/");
        CookieUtil.addCookie(response, JwtConstants.REFRESH_TOKEN_COOKIE_NAME, refresh, JwtConstants.REFRESH_TOKEN_EXP_SECONDS, "/api/auth/reissue");
    }
}
