package com.guideon.config;

import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import com.guideon.security.config.SecurityConfig;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;
import javax.servlet.Filter;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    @Override
    public void onStartup(ServletContext servletContext) throws ServletException {
        // 0) .env 경로 결정 (VM 옵션이나 환경변수로 지정 가능)
        String envDir = System.getProperty("guideon.env.dir");
        if (envDir == null || envDir.isEmpty()) {
            envDir = System.getenv("GUIDEON_ENV_DIR");
        }

        System.out.println("[ENV] guideon.env.dir=" + System.getProperty("guideon.env.dir"));

        // 1) .env -> System properties 선주입 (스프링 컨텍스트 생성 전에!)
        DotenvBuilder builder = Dotenv.configure()
                .ignoreIfMissing()
                .filename(".env");
        if (envDir != null && !envDir.isEmpty()) {
            builder = builder.directory(envDir);
        }
        builder.systemProperties().load();

        System.out.println("[ENV] .env exists? " + new java.io.File(
                System.getProperty("guideon.env.dir"), ".env").exists());

        // (선택) 디버그 로그 — 값 들어왔는지 1번만 확인
        System.out.println("[ENV] DB_URL=" + System.getProperty("DB_URL"));

        // 2) 이제 평소처럼 스프링 부팅
        super.onStartup(servletContext);
    }

    @Override
    protected Class<?>[] getRootConfigClasses() {
        // 순서: EnvConfig -> RootConfig -> SecurityConfig -> RedisConfig
        return new Class[] { RootConfig.class, SecurityConfig.class, RedisConfig.class };
    }

    @Override
    protected Class<?>[] getServletConfigClasses() {
        return new Class[] { ServletConfig.class, SwaggerConfig.class };
    }

    @Override
    protected String[] getServletMappings() {
        return new String[] { "/" };
    }

    protected Filter[] getServletFilters() {
        // UTF-8 문자 인코딩 필터 생성 및 설정
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");       // 요청 데이터 UTF-8 디코딩
        characterEncodingFilter.setForceEncoding(true);     // 응답 데이터도 UTF-8 강제 인코딩

        return new Filter[] { characterEncodingFilter };
    }
}