package com.guideon.common.redis;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    public void blacklistToken(String token, long expirationMillis) {
        if (expirationMillis <= 0) { redisTemplate.delete(token); return; }
        redisTemplate.opsForValue().set(token, "logout", expirationMillis, TimeUnit.MILLISECONDS);
    }

    public boolean isBlacklisted(String token) {
        return exists(token);
    }

    public String get(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    // 값 저장 (prefix:key 형식, TTL 설정)
    public void set(String key, String value, int timeoutMinutes) {
        redisTemplate.opsForValue().set(key, value, timeoutMinutes, TimeUnit.MINUTES);
    }

    // key 존재하지 않을 시 저장
    public boolean setIfAbsent(String key, String value, Duration ttl) {
        return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(key, value, ttl));
    }

    public boolean delete(String key) {
        return Boolean.TRUE.equals(redisTemplate.delete(key));
    }

    // 값 검증 (인증번호 비교)
    public boolean verify(String key, String inputValue) {
        String stored = get(key);
        return inputValue != null && inputValue.equals(stored);
    }

    public boolean isVerified(String type) {
        return exists(RedisKeyUtil.verified(type));
    }

    public boolean exists(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
