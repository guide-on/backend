package com.guideon.config;

import lombok.extern.log4j.Log4j2;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import javax.sql.DataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * DataSource 및 커넥션 풀 테스트 클래스
 */
@ExtendWith(SpringExtension.class)              // JUnit5에서 Spring 테스트 지원
@ContextConfiguration(classes = {RootConfig.class})  // 테스트에 사용할 설정 클래스
@Log4j2
class RootConfigTest {

    @Autowired  // DataSource Bean 의존성 주입
    private DataSource dataSource;

    @Autowired  // SqlSessionFactory Bean 의존성 주입
    private SqlSessionFactory sqlSessionFactory;

    @Test
    @DisplayName("DataSource 연결이 된다.")
    public void dataSource() throws SQLException {
        // 커넥션 풀에서 연결 가져오기
        try (Connection con = dataSource.getConnection()) {
            log.info("=== DataSource 커넥션 풀 연결 성공 ===");
            log.info("연결 객체: {}", con);
            log.info("연결 클래스: {}", con.getClass().getName());

            // 커넥션이 HikariCP Proxy인지 확인
            if (con.getClass().getName().contains("Hikari")) {
                log.info("✅ HikariCP 커넥션 풀이 정상 작동중입니다.");
            }

        } catch (SQLException e) {
            log.error("❌ DataSource 연결 실패: {}", e.getMessage());
            throw e;
        }
    }

    @Test
    @DisplayName("sqlSessionFactory 동작 확인")
    public void testSqlSessionFactory() {
        try (
                // SqlSession 생성 및 자동 해제 (try-with-resources)
                SqlSession session = sqlSessionFactory.openSession();
                Connection con = session.getConnection()
        ) {
            log.info("SqlSession: " + session);
            log.info("Connection: " + con);
        } catch (Exception e) {

            // import static org.junit.jupiter.api.Assertions.fail;
            fail(e.getMessage());
        }
    }

}