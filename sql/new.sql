-- reset_all.sql
-- 전체 드롭 후 최신 스키마로 재생성 + 기본 시드

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ===== DROP TABLES (FK 의존도 역순) =====
DROP TABLE IF EXISTS post_hashtag;
DROP TABLE IF EXISTS post_like;
DROP TABLE IF EXISTS post_bookmark;
DROP TABLE IF EXISTS comment;
DROP TABLE IF EXISTS post_image;
DROP TABLE IF EXISTS post;

DROP TABLE IF EXISTS hashtag;

DROP TABLE IF EXISTS business_profile;
DROP TABLE IF EXISTS individual_interest_industry_map;
DROP TABLE IF EXISTS individual_interest_region_map;
DROP TABLE IF EXISTS member_auth;
DROP TABLE IF EXISTS member;

DROP TABLE IF EXISTS industry_code;
DROP TABLE IF EXISTS region_code;

SET FOREIGN_KEY_CHECKS = 1;

-- =====================================================================
-- =                             (RE)CREATE                             =
-- =====================================================================

-- =========================================================
-- 코드 테이블 (지역/업종)
-- =========================================================
CREATE TABLE region_code (
                             sgg_code   VARCHAR(10) PRIMARY KEY,         -- 시군구 코드 (예: 41173)
                             sido       VARCHAR(20)  NOT NULL,           -- 경기도
                             sgg        VARCHAR(40)  NOT NULL,           -- 안양시 동안구
                             updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE industry_code (
                               ksic_code   VARCHAR(10) PRIMARY KEY,        -- KSIC 코드 (예: 56111)
                               name_kr     VARCHAR(100) NOT NULL,          -- 업종명
                               updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 회원 (member.sql 최신 스키마)
-- =========================================================
CREATE TABLE member (
                        member_id          BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_type        VARCHAR(20)  NOT NULL,       -- ex) INDIVIDUAL / SOLE_PROPRIETOR
                        email              VARCHAR(120) NOT NULL UNIQUE,

                        nickname VARCHAR(120) GENERATED ALWAYS AS (
                            CASE
                                WHEN INSTR(email, '@') = 0 THEN NULL
                                WHEN CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)) <= 2
                                    THEN REPEAT('*', CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)))
                                ELSE CONCAT(
                                    LEFT(SUBSTRING_INDEX(email, '@', 1), 2),
                                        REPEAT('*', CHAR_LENGTH(SUBSTRING_INDEX(email, '@', 1)) - 2)
                                     )
                                END
                            ) STORED COMMENT '이메일 로컬파트 마스킹',

                        password           VARCHAR(255) NOT NULL,
                        name               VARCHAR(60)  NOT NULL,
                        birth              DATE         NOT NULL COMMENT '생년월일',
                        gender             CHAR(1)      NOT NULL CHECK (gender IN ('M','F')) COMMENT '성별(M/F)',
                        phone              VARCHAR(20)  NOT NULL UNIQUE,

                        residence_sgg_code VARCHAR(10) NULL,           -- 거주지(선택). 표시는 region_code 조인
                        CONSTRAINT fk_member_res FOREIGN KEY (residence_sgg_code)
                            REFERENCES region_code(sgg_code),

                        created_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at         TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE member_auth (
                             member_id BIGINT NOT NULL COMMENT '회원 고유 ID (PK)',
                             auth      VARCHAR(20) NOT NULL COMMENT '권한명(ROLE_MEMBER, ROLE_ADMIN 등)',
                             PRIMARY KEY (member_id, auth),
                             CONSTRAINT fk_authorities_members
                                 FOREIGN KEY (member_id) REFERENCES member(member_id)
                                     ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='회원 권한 테이블';

-- =========================================================
-- 개인회원: 관심 지역/업종 (개별 매핑만 유지)
-- =========================================================
CREATE TABLE individual_interest_region_map (
                                                member_id BIGINT      NOT NULL,
                                                sgg_code  VARCHAR(10) NOT NULL,
                                                PRIMARY KEY (member_id, sgg_code),
                                                CONSTRAINT fk_iirm_member FOREIGN KEY (member_id)
                                                    REFERENCES member(member_id) ON DELETE CASCADE,
                                                CONSTRAINT fk_iirm_region FOREIGN KEY (sgg_code)
                                                    REFERENCES region_code(sgg_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE individual_interest_industry_map (
                                                  member_id BIGINT      NOT NULL,
                                                  ksic_code VARCHAR(10) NOT NULL,
                                                  PRIMARY KEY (member_id, ksic_code),
                                                  CONSTRAINT fk_iiim_member FOREIGN KEY (member_id)
                                                      REFERENCES member(member_id) ON DELETE CASCADE,
                                                  CONSTRAINT fk_iiim_ksic FOREIGN KEY (ksic_code)
                                                      REFERENCES industry_code(ksic_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 사업자회원 (사업자 정보)
-- =========================================================
CREATE TABLE business_profile (
                                  member_id         BIGINT       PRIMARY KEY,
                                  biz_reg_no        CHAR(10)     NOT NULL UNIQUE CHECK (biz_reg_no REGEXP '^[0-9]{10}$'),
  biz_name          VARCHAR(100) NOT NULL,
  opening_date      DATE         NOT NULL,
  ksic_code         VARCHAR(10)  NULL,
  CONSTRAINT fk_biz_ksic FOREIGN KEY (ksic_code)
    REFERENCES industry_code(ksic_code),

  business_sgg_code VARCHAR(10)  NOT NULL,  -- 지역 API 코드만 보관
  CONSTRAINT fk_biz_sgg  FOREIGN KEY (business_sgg_code)
    REFERENCES region_code(sgg_code),

  addr_road         VARCHAR(120) NOT NULL,  -- 예: 관악대로 297-7
  addr_detail       VARCHAR(120) NULL,      -- 예: 1층

  created_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
  updated_at        TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

  CONSTRAINT fk_biz_member FOREIGN KEY (member_id)
    REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- =========================================================
-- 커뮤니티 (community.sql 최신)
-- =========================================================
CREATE TABLE post (
                      id BIGINT AUTO_INCREMENT PRIMARY KEY,
                      member_id BIGINT NOT NULL,
                      category ENUM('CASE','FREE') NOT NULL,
                      free_type ENUM('QUESTION','PROMO','TIP','OTHER') NULL,
                      title VARCHAR(200) NOT NULL,
                      content MEDIUMTEXT NOT NULL,
                      thumbnail_url VARCHAR(500) NULL,
                      view_count INT NOT NULL DEFAULT 0,
                      like_count INT NOT NULL DEFAULT 0,
                      bookmark_count INT NOT NULL DEFAULT 0,
                      comment_count INT NOT NULL DEFAULT 0,
                      is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                      created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                      CONSTRAINT fk_post_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE,
                      KEY ix_post_category_created (category, created_at),
                      KEY ix_post_popular (category, bookmark_count, view_count, like_count),
                      KEY ix_post_member_created (member_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_image (
                            id BIGINT AUTO_INCREMENT PRIMARY KEY,
                            post_id BIGINT NOT NULL,
                            image_url VARCHAR(500) NOT NULL,
                            sort_order INT NOT NULL DEFAULT 1,
                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                            CONSTRAINT fk_post_image FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                            KEY ix_post_image_order (post_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE hashtag (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         name VARCHAR(100) NOT NULL,
                         tag_type ENUM('SECTOR','POST_TYPE','GENERIC') NOT NULL,
                         code VARCHAR(30) NULL,
                         ref_ksic_code VARCHAR(10) NULL,
                         is_active TINYINT(1) NOT NULL DEFAULT 1,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_hashtag_ksic FOREIGN KEY (ref_ksic_code) REFERENCES industry_code(ksic_code),
                         UNIQUE KEY uk_hashtag_name_type (name, tag_type),
                         UNIQUE KEY uk_hashtag_type_code (tag_type, code),
                         KEY ix_hashtag_type (tag_type),
                         KEY ix_hashtag_code (code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_hashtag (
                              post_id BIGINT NOT NULL,
                              hashtag_id BIGINT NOT NULL,
                              created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              PRIMARY KEY (post_id, hashtag_id),
                              CONSTRAINT fk_ph_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                              CONSTRAINT fk_ph_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_like (
                           post_id BIGINT NOT NULL,
                           member_id BIGINT NOT NULL,
                           created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                           PRIMARY KEY (post_id, member_id),
                           CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                           CONSTRAINT fk_like_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE post_bookmark (
                               post_id BIGINT NOT NULL,
                               member_id BIGINT NOT NULL,
                               created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               PRIMARY KEY (post_id, member_id),
                               CONSTRAINT fk_bm_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                               CONSTRAINT fk_bm_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comment (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         post_id BIGINT NOT NULL,
                         member_id BIGINT NOT NULL,
                         parent_comment_id BIGINT NULL,
                         content TEXT NOT NULL,
                         depth TINYINT NOT NULL DEFAULT 0,
                         is_deleted TINYINT(1) NOT NULL DEFAULT 0,
                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
                         CONSTRAINT fk_c_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                         CONSTRAINT fk_c_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE,
                         CONSTRAINT fk_c_parent FOREIGN KEY (parent_comment_id) REFERENCES comment(id) ON DELETE CASCADE,
                         KEY ix_comment_post_time (post_id, created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- POST_TYPE 해시태그 시드
INSERT INTO hashtag (name, tag_type, code) VALUES
                                               ('질문','POST_TYPE','QUESTION'),
                                               ('홍보','POST_TYPE','PROMO'),
                                               ('마케팅팁','POST_TYPE','TIP'),
                                               ('기타','POST_TYPE','OTHER')
    ON DUPLICATE KEY UPDATE name=VALUES(name);

-- =========================================================
-- 기본 회원 시드 (member.sql)
--  ※ AUTO_INCREMENT 값은 환경에 따라 달라질 수 있음
-- =========================================================
INSERT INTO member (email, password, member_type, created_at, updated_at, name, phone, birth, gender)
VALUES
    ('admin@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'SOLE_PROPRIETOR', NOW(), NOW(), '홍길동', '01011112222', '2000-05-10', 'M'),
    ('test1@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'INDIVIDUAL', NOW(), NOW(), '김영희', '01033334444', '2009-10-16', 'F'),
    ('test2@example.com', '$2a$10$EsIMfxbJ6NuvwX7MDj4WqOYFzLU9U/lddCyn0nic5dFo3VfJYrXYC', 'INDIVIDUAL', NOW(), NOW(), '김철수', '01055555555', '2009-10-16', 'M');

-- 권한 시드 (위 INSERT로 들어간 PK 값이 1,2,3이라고 가정)
INSERT INTO member_auth (member_id, auth)
VALUES
    (3, 'ROLE_MEMBER'),
    (2, 'ROLE_MEMBER'),
    (1, 'ROLE_ADMIN');
