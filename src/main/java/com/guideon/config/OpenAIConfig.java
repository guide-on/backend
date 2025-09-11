package com.guideon.config;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenAIConfig {
    @Bean
    public OpenAIClient openAIClient() {
        // OPENAI_API_KEY 환경변수 자동 인식
        return OpenAIOkHttpClient.fromEnv();
    }
}
