package com.guideon.config;

import io.lettuce.core.ClientOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.data.redis.connection.*;
import org.springframework.data.redis.connection.lettuce.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Configuration
@PropertySource({"classpath:/application.properties"})
public class RedisConfig {
    @Value("${spring.redis.host}")
    private String redisHost;

    @Value("${spring.redis.port}")
    private int redisPort;

    @Value("${spring.redis.password:}")
    private String password;

    @Value("${spring.redis.timeout-ms:3000}")
    private long timeoutMs;

    @Value("${spring.redis.ssl:false}")
    private boolean useSsl;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // 서버 설정
        RedisStandaloneConfiguration server = new RedisStandaloneConfiguration(redisHost, redisPort);
        if (StringUtils.hasText(password)) {
            server.setPassword(RedisPassword.of(password));
        }

        // 클라이언트 옵션 (타임아웃/자동 재연결/선택적 SSL)
        LettuceClientConfiguration.LettuceClientConfigurationBuilder builder = LettuceClientConfiguration.builder()
                .commandTimeout(Duration.ofMillis(timeoutMs))
                .shutdownTimeout(Duration.ZERO)
                .clientOptions(ClientOptions.builder().autoReconnect(true).build());
        if (useSsl) builder.useSsl();

        return new LettuceConnectionFactory(server, builder.build());
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory cf) {
        return new StringRedisTemplate(cf);
    }
}
