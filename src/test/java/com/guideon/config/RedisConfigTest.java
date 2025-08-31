package com.guideon.config;

import lombok.extern.log4j.Log4j2;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = {RedisConfig.class})
@Log4j2
class RedisConfigTest {

    @Autowired
    RedisConnectionFactory connectionFactory;

    @Autowired
    StringRedisTemplate redis;

    private final String key = "it:health";

    @BeforeEach
    void cleanBefore() {
        redis.delete(key);
    }

    @AfterEach
    void cleanAfter() {
        redis.delete(key);
    }

    @Test
    @DisplayName("PING 호출 시 PONG을 반환하는지 확인")
    void redisConnectionFactory() {
        assertNotNull(connectionFactory, "RedisConnectionFactory 주입 실패");
        String pong;
        try (var conn = connectionFactory.getConnection()) {
            assertNotNull(conn, "Redis Connection이 null");
            pong = conn.ping();
        }
        assertNotNull(pong, "PING 결과가 null");
        assertTrue("PONG".equalsIgnoreCase(pong), "PING 결과가 PONG이 아님: " + pong);
    }

    @Test
    @DisplayName("문자열 set/get이 정상 동작 확인")
    void 문자열_저장_후_조회_값_일치() {
        String value = "ok";
        redis.opsForValue().set(key, value);
        String got = redis.opsForValue().get(key);
        assertEquals(value, got, "저장/조회 값이 다름");
    }
}