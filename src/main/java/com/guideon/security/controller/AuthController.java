package com.guideon.security.controller;

import com.guideon.common.redis.RedisKeyUtil;
import com.guideon.common.redis.RedisService;
import com.guideon.member.service.MemberService;
import com.guideon.security.account.domain.MemberVO;
import com.guideon.security.account.dto.UserInfoDTO;
import com.guideon.security.util.CookieUtil;
import com.guideon.security.util.JwtConstants;
import com.guideon.security.util.JwtProcessor;
import com.guideon.security.util.LoginUserProvider;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Api(tags = "인증 관리 API", description = "JWT 토큰 기반 인증, 로그아웃, 토큰 재발급")
public class AuthController {

    private final RedisService redisService;
    private final JwtProcessor jwtProcessor;
    private final MemberService memberService;
    private final LoginUserProvider loginUserProvider;

    @PostMapping("/logout")
    @ApiOperation(value = "로그아웃", notes = "Access Token을 블랙리스트에 등록하고 Refresh Token을 삭제.")
    public ResponseEntity<String> logout(HttpServletRequest request, HttpServletResponse response) {
        // Access Token blacklist 등록
        String token = jwtProcessor.extractAccessToken(request);
        if (StringUtils.hasText(token) && jwtProcessor.validateAccessToken(token)) {
            long remaining = jwtProcessor.getRemainingExpiration(token);
            redisService.blacklistToken(token, remaining);
            CookieUtil.deleteCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, "/");
        }

        // refresh 토큰 삭제
        String refreshToken = jwtProcessor.extractRefreshToken(request);
        if (StringUtils.hasText(refreshToken) && jwtProcessor.validateRefreshToken(refreshToken)) {
            Long memberId = jwtProcessor.getMemberId(refreshToken);
            redisService.delete(RedisKeyUtil.refreshToken(memberId));
            CookieUtil.deleteCookie(response, JwtConstants.REFRESH_TOKEN_COOKIE_NAME, "/api/auth/reissue");
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/me")
    @ApiOperation(value = "현재 로그인 사용자 정보 조회", notes = "현재 로그인된 사용자의 기본 정보를 반환.")
    public ResponseEntity<UserInfoDTO> getMe() {
        MemberVO member = loginUserProvider.getLoginUser();
        if (member == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(UserInfoDTO.of(member));
    }

    @PostMapping("/reissue")
    @ApiOperation(value = "Access Token 재발급", notes = "Refresh Token을 사용하여 새로운 Access Token을 발급.")
    public ResponseEntity<?> reissue(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = jwtProcessor.extractRefreshToken(request); // 쿠키에서 추출

        if (!StringUtils.hasText(refreshToken) || !jwtProcessor.validateRefreshToken(refreshToken)) {
            throw new IllegalStateException("유효하지 않은 Refresh Token입니다");
        }

        Long memberId = jwtProcessor.getMemberId(refreshToken);
        String redisKey = RedisKeyUtil.refreshToken(memberId);
        String savedRefreshToken = redisService.get(redisKey);

        // 탈취 감지: 저장된 토큰과 일치하지 않으면 로그아웃 처리
        if (!refreshToken.equals(savedRefreshToken)) {
            redisService.delete(redisKey); // 탈취된 토큰 무효화
            throw new IllegalStateException("Refresh Token 정보가 일치하지 않습니다");
        }

        String email = memberService.get(memberId, null).getEmail();
        if (!StringUtils.hasText(email)) {
            throw new IllegalStateException("존재하지 않는 사용자입니다");
        }
        String newAccessToken = jwtProcessor.generateAccessToken(memberId, email);
        CookieUtil.addCookie(response, JwtConstants.ACCESS_TOKEN_COOKIE_NAME, newAccessToken, JwtConstants.ACCESS_TOKEN_EXP_SECONDS, "/");

        return ResponseEntity.ok().build();
    }
}
