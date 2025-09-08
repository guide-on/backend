SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

CREATE TABLE IF NOT EXISTS post (
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

CREATE TABLE IF NOT EXISTS post_image (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          post_id BIGINT NOT NULL,
                                          image_url VARCHAR(500) NOT NULL,
                                          sort_order INT NOT NULL DEFAULT 1,
                                          created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                          CONSTRAINT fk_post_image FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                          KEY ix_post_image_order (post_id, sort_order)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS hashtag (
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

CREATE TABLE IF NOT EXISTS post_hashtag (
                                            post_id BIGINT NOT NULL,
                                            hashtag_id BIGINT NOT NULL,
                                            created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                            PRIMARY KEY (post_id, hashtag_id),
                                            CONSTRAINT fk_ph_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                            CONSTRAINT fk_ph_hashtag FOREIGN KEY (hashtag_id) REFERENCES hashtag(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_like (
                                         post_id BIGINT NOT NULL,
                                         member_id BIGINT NOT NULL,
                                         created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                         PRIMARY KEY (post_id, member_id),
                                         CONSTRAINT fk_like_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                         CONSTRAINT fk_like_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS post_bookmark (
                                             post_id BIGINT NOT NULL,
                                             member_id BIGINT NOT NULL,
                                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                             PRIMARY KEY (post_id, member_id),
                                             CONSTRAINT fk_bm_post FOREIGN KEY (post_id) REFERENCES post(id) ON DELETE CASCADE,
                                             CONSTRAINT fk_bm_member FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE IF NOT EXISTS comment (
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

-- ===== SECTOR 해시태그 시드 =====
INSERT INTO hashtag (name, tag_type, code, is_active) VALUES
                                                          ('요식업',  'SECTOR', 'FOOD',     1),
                                                          ('미용',    'SECTOR', 'BEAUTY',   1),
                                                          ('의류',    'SECTOR', 'APPAREL',  1),
                                                          ('소매업',  'SECTOR', 'RETAIL',   1),
                                                          ('서비스업','SECTOR', 'SERVICE',  1)
    ON DUPLICATE KEY UPDATE
                         name = VALUES(name),
                         is_active = VALUES(is_active);


-- ===== GENERIC 해시태그 샘플 (선택) =====
INSERT INTO hashtag (name, tag_type, code, is_active) VALUES
                                                          ('정부지원', 'GENERIC', NULL, 1),
                                                          ('세무',     'GENERIC', NULL, 1),
                                                          ('노무',     'GENERIC', NULL, 1),
                                                          ('마케팅',   'GENERIC', NULL, 1),
                                                          ('창업',     'GENERIC', NULL, 1),
                                                          ('운영팁',   'GENERIC', NULL, 1)
ON DUPLICATE KEY UPDATE
                     name = VALUES(name),
                     is_active = VALUES(is_active);



SET FOREIGN_KEY_CHECKS = 1;
