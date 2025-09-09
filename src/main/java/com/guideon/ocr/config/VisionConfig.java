package com.guideon.ocr.config;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class VisionConfig {
    @Bean(destroyMethod = "close")
    public ImageAnnotatorClient imageAnnotatorClient() throws Exception {
        return ImageAnnotatorClient.create(); // ADC(환경변수) 사용
    }
}
