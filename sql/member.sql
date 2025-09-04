CREATE TABLE member (
                        member_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_type         VARCHAR(20)  NOT NULL,       -- ex) INDIVIDUAL / SOLE_PROPRIETOR (백엔드 enum 검증)
                        email               VARCHAR(120) NOT NULL UNIQUE,
                        nickname            VARCHAR(120) GENERATED ALWAYS AS (
                            CASE
                                WHEN INSTR(email, '@') = 0
                                    THEN NULL
                                WHEN CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)) <= 2
                                    THEN REPEAT('*', CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)))
                                ELSE CONCAT(
                                    LEFT(SUBSTRING_INDEX(email, '@', 1), 2),
                                        REPEAT('*', CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)) - 2)
                                     )
                                END
                            ) STORED COMMENT '이메일 로컬파트 마스킹',
                        password            VARCHAR(255) NOT NULL,
                        name                VARCHAR(60)  NOT NULL,
                        birth               DATE         NOT NULL COMMENT '생년월일',
                        gender              CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F')) COMMENT '성별(M/F)',
                        phone               VARCHAR(20)  NOT NULL UNIQUE,

                        residence_sgg_code  VARCHAR(10) NULL,            -- 거주지(선택). 표시는 region_code 조인
                        CONSTRAINT fk_member_res FOREIGN KEY (residence_sgg_code)
                            REFERENCES region_code(sgg_code),

                        created_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at          TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE member_auth (
                             member_id   BIGINT NOT NULL COMMENT '회원 고유 ID (PK)',
                             auth        VARCHAR(20) NOT NULL COMMENT '권한명(ROLE_MEMBER, ROLE_ADMIN 등)',
                             PRIMARY KEY (member_id, auth),
                             CONSTRAINT fk_authorities_members
                                 FOREIGN KEY (member_id) REFERENCES member (member_id)
                                     ON DELETE CASCADE
) COMMENT='회원 권한 테이블';

-- 회원 기본 정보
INSERT INTO member (email, password, member_type, created_at, updated_at, name, phone, birth, gender)
VALUES
    ('admin@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'SOLE_PROPRIETOR', NOW(), NOW(), '홍길동', '01011112222', '2000-05-10', 'M'),
    ('test1@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'INDIVIDUAL', NOW(), NOW(), '김영희', '01033334444', '2009-10-16', 'F'),
    ('test2@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'INDIVIDUAL', NOW(), NOW(), '김철수', '01055555555', '2009-10-16', 'M');

-- 회원 권한 정보
INSERT INTO member_auth (member_id, auth)
VALUES
    (3, 'ROLE_MEMBER'),
    (2, 'ROLE_MEMBER'),
    (1, 'ROLE_ADMIN');