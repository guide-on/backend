package com.guideon.config;

import com.guideon.ocr.config.VisionConfig;
import io.github.cdimascio.dotenv.Dotenv;
import io.github.cdimascio.dotenv.DotenvBuilder;
import javax.servlet.*;
import com.guideon.security.config.SecurityConfig;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

public class WebConfig extends AbstractAnnotationConfigDispatcherServletInitializer {

    // 파일 업로드 설정 상수
    private static final long MAX_FILE_SIZE = 1024 * 1024 * 10L;      // 10MB
    private static final long MAX_REQUEST_SIZE = 1024 * 1024 * 20L;   // 20MB
    private static final int FILE_SIZE_THRESHOLD = 1024 * 1024 * 5;   // 5MB

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
        return new Class[] { RootConfig.class, SecurityConfig.class, RedisConfig.class, MailConfig.class, VisionConfig.class, OpenAIConfig.class };
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

    @Override
    protected void customizeRegistration(ServletRegistration.Dynamic registration) {
        registration.setInitParameter("throwExceptionIfNoHandlerFound", "true");

        registration.setInitParameter("allowCasualMultipartParsing", "true");

        final String UPLOAD_LOCATION = System.getProperty("UPLOAD_BASE_PATH", "/tmp/uploads");

        // 디버그 로그 추가
        System.out.println("[UPLOAD] Using location: " + UPLOAD_LOCATION);

        registration.setMultipartConfig(new MultipartConfigElement(
                UPLOAD_LOCATION,        // 업로드 파일 임시 저장 디렉토리
                MAX_FILE_SIZE,          // 업로드 가능한 파일 하나의 최대 크기
                MAX_REQUEST_SIZE,       // 업로드 가능한 전체 최대 크기(여러 파일 업로드)
                FILE_SIZE_THRESHOLD     // 메모리 파일의 최대 크기(임계값)
        ));
    }
}