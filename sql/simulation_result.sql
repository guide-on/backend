-- MySQL 8.x 기준
CREATE TABLE simulation_result (
                                   session_id                BIGINT PRIMARY KEY COMMENT 'loan_sessions.id와 1:1 매핑',
                                   member_id                 BIGINT       NOT NULL,
                                   fund_name                 VARCHAR(200) NULL  COMMENT '대상 자금/상품명',

    -- 전체 진행 메타
                                   current_step              VARCHAR(12)  NOT NULL COMMENT '현재 단계(DOCS/CREDIT/PLAN/RESULT)',
                                   overall_status            VARCHAR(12)  NOT NULL COMMENT '진행 상태(IN_PROGRESS/COMPLETED/STOPPED)',
                                   started_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   updated_at                DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    -- 서류 단계
                                   doc_session_status        VARCHAR(12)  NULL  COMMENT '서류 세션 상태(IN_PROGRESS/COMPLETED)',

    -- 신용평가 단계
                                   total_credit_score        INT          NULL  COMMENT '0~1000',
                                   hybrid_credit_score       INT          NULL,
                                   traditional_credit_score  INT          NULL,
                                   credit_last_updated       DATETIME     NULL,

    -- 사업계획서 단계
                                   plan_total_score          DECIMAL(5,2) NULL  COMMENT '0~100',

    -- 생성 컬럼(가중합 계산)
                                   doc_score_pct             DECIMAL(5,2) AS (
                                       CASE WHEN doc_session_status = 'COMPLETED' THEN 100.00 ELSE 0.00 END
                                       ) STORED,
                                   credit_score_pct          DECIMAL(5,2) AS (
                                       CASE WHEN total_credit_score IS NULL THEN 0.00 ELSE ROUND(total_credit_score / 10, 2) END
                                       ) STORED,
                                   plan_score_pct            DECIMAL(5,2) AS (COALESCE(plan_total_score, 0.00)) STORED,
                                   total_probability_pct     DECIMAL(6,3) AS (
                                       ROUND(doc_score_pct*0.60 + credit_score_pct*0.30 + plan_score_pct*0.10, 3)
                                       ) STORED COMMENT '총 승인 가능 확률(%)',

                                   CONSTRAINT fk_sim_result_member
                                       FOREIGN KEY (member_id) REFERENCES member(member_id) ON DELETE RESTRICT,
                                   CONSTRAINT fk_sim_result_session
                                       FOREIGN KEY (session_id) REFERENCES loan_sessions(id) ON DELETE CASCADE,

                                   CONSTRAINT chk_current_step
                                       CHECK (current_step IN ('DOCS','CREDIT','PLAN','RESULT')),
                                   CONSTRAINT chk_overall_status
                                       CHECK (overall_status IN ('IN_PROGRESS','COMPLETED','STOPPED')),
                                   CONSTRAINT chk_doc_status
                                       CHECK (doc_session_status IN ('IN_PROGRESS','COMPLETED') OR doc_session_status IS NULL)
);

-- 조회 최적화 인덱스
CREATE INDEX ix_sim_result_member ON simulation_result(member_id);
CREATE INDEX ix_sim_result_status ON simulation_result(overall_status, current_step);