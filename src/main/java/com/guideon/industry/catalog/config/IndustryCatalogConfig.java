package com.guideon.industry.catalog.config;

import com.guideon.industry.catalog.infra.ClasspathIndustryTagCatalog;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

@Configuration
public class IndustryCatalogConfig {
    @Bean
    public ClasspathIndustryTagCatalog industryTagCatalog(
            @Value("${catalog.industry.tags.path:classpath:config/industry-tags.ko.json}") String path,
            ResourceLoader loader) {
        Resource res = path.startsWith("classpath:")
                ? new ClassPathResource(path.substring("classpath:".length()))
                : loader.getResource(path);
        return new ClasspathIndustryTagCatalog(res);
    }
}