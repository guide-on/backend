package com.guideon.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Jackson JSON 직렬화/역직렬화 설정
 */
@Configuration
public class JacksonConfig implements WebMvcConfigurer {

    // 날짜 포맷 상수
    private static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern(DATE_TIME_FORMAT);

    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Java 8 시간 타입 지원 모듈 등록
        JavaTimeModule javaTimeModule = new JavaTimeModule();

        // LocalDateTime을 원하는 형식으로 직렬화
        javaTimeModule.addSerializer(LocalDateTime.class, new LocalDateTimeSerializer(DATE_TIME_FORMATTER));

        mapper.registerModule(javaTimeModule);

        // 배열 형태가 아닌 ISO 형태로 출력 (WRITE_DATES_AS_TIMESTAMPS 비활성화)
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        // null 값은 출력하지 않음 (선택사항)
        // mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);

        return mapper;
    }

    @Override
    public void configureMessageConverters(List<HttpMessageConverter<?>> converters) {
        // Jackson HTTP 메시지 컨버터 설정
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setObjectMapper(objectMapper());

        // 첫 번째로 추가하여 우선순위 보장
        converters.add(0, converter);
    }
}