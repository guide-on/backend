package com.guideon.funds.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정책지원금 도메인 객체 (support_fund 테이블)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Funds {
    private Long id;                // 정책지원금 ID (support_fund.id)
    private Integer year;           // 연도
    private String categoryCode;    // 카테고리 코드
    private String name;            // 정책지원금 명
    private String status;          // 상태
    private String target;          // 대상
    private String purpose;         // 목적
    private String rate;            // 지원율/금리
    private String term;            // 지원기간
    private String limitAmount;     // 지원한도
    private LocalDateTime createdAt; // 생성일
    private LocalDateTime updatedAt; // 수정일
}
