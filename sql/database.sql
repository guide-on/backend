-- =========================================================
-- 코드 테이블 (지역/업종)
-- =========================================================
CREATE TABLE region_code (
                             sgg_code   VARCHAR(10) PRIMARY KEY,   -- 시군구 코드 (예: 41173)
                             sido       VARCHAR(20)  NOT NULL,     -- 경기도
                             sgg        VARCHAR(40)  NOT NULL,     -- 안양시 동안구
                             updated_at TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE industry_code (
                               ksic_code   VARCHAR(10) PRIMARY KEY,  -- KSIC 코드 (예: 56111)
                               name_kr     VARCHAR(100) NOT NULL,    -- 업종명
                               updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- =========================================================
-- 회원 (공통: 로그인/기본정보/동의)
-- =========================================================
CREATE TABLE member (
                        member_id           BIGINT PRIMARY KEY AUTO_INCREMENT,
                        member_type_code    VARCHAR(20)  NOT NULL,       -- ex) INDIVIDUAL / SOLE_PROPRIETOR (백엔드 enum 검증)
                        email               VARCHAR(120) NOT NULL UNIQUE,
                        password            VARCHAR(255) NOT NULL,
                        name                VARCHAR(60)  NOT NULL,
                        birth_date          DATE         NOT NULL COMMENT '생년월일',
                        gender              CHAR(1)      NOT NULL CHECK (gender IN ('M', 'F')) COMMENT '성별(M/F)',
                        phone               VARCHAR(20)  NOT NULL UNIQUE,

                        residence_sgg_code  VARCHAR(10) NULL,            -- 거주지(선택). 표시는 region_code 조인
                        CONSTRAINT fk_member_res FOREIGN KEY (residence_sgg_code)
                            REFERENCES region_code(sgg_code),

    -- 필수 동의(최소 보관)
                        terms_version       VARCHAR(20)  NOT NULL,
                        privacy_version     VARCHAR(20)  NOT NULL,
                        terms_agreed_at     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        privacy_agreed_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        status_code         VARCHAR(15)  NOT NULL DEFAULT 'ACTIVE',  -- ex) ACTIVE/SUSPENDED/DELETED
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
);

CREATE TABLE individual_interest_industry_map (
                                                  member_id BIGINT      NOT NULL,
                                                  ksic_code VARCHAR(10) NOT NULL,
                                                  PRIMARY KEY (member_id, ksic_code),
                                                  CONSTRAINT fk_iiim_member FOREIGN KEY (member_id)
                                                      REFERENCES member(member_id) ON DELETE CASCADE,
                                                  CONSTRAINT fk_iiim_ksic FOREIGN KEY (ksic_code)
                                                      REFERENCES industry_code(ksic_code)
);

-- =========================================================
-- 사업자회원 (사업자 정보)
--  - BRN 평문 10자리(하이픈 제거) 저장 + 유니크 + 체크
--  - 주소는 "시군구 코드"만 저장(표시는 조인)
-- =========================================================
CREATE TABLE business_profile (
                                  member_id              BIGINT       PRIMARY KEY,

                                  biz_reg_no             CHAR(10)     NOT NULL UNIQUE CHECK (biz_reg_no REGEXP '^[0-9]{10}$'),

                                  biz_name               VARCHAR(100) NOT NULL,
                                  opening_date           DATE         NOT NULL,
                                  ksic_code              VARCHAR(10)  NULL,
                                  CONSTRAINT fk_biz_ksic FOREIGN KEY (ksic_code)
                                      REFERENCES industry_code(ksic_code),

                                  business_sgg_code      VARCHAR(10)  NOT NULL,  -- 지역 API 코드만 보관
                                  CONSTRAINT fk_biz_sgg  FOREIGN KEY (business_sgg_code)
                                      REFERENCES region_code(sgg_code),

                                  addr_road              VARCHAR(120) NOT NULL,  -- 예: 관악대로 297-7
                                  addr_detail            VARCHAR(120) NULL,      -- 예: 1층

                                  created_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                  updated_at             TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                                  CONSTRAINT fk_biz_member FOREIGN KEY (member_id)
                                      REFERENCES member(member_id) ON DELETE CASCADE
);