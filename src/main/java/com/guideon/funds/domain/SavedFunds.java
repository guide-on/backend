package com.guideon.funds.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 북마크한 정책지원금 도메인 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedFunds {
    private Long savedFundsId;      // 북마크 ID
    private Long memberId;          // 회원 ID
    private Long fundsId;           // 정책지원금 ID (support_fund.id 참조)
    private LocalDateTime createdAt; // 북마크 생성일
    
    // 조인을 위한 Funds 정보
    private Funds funds;
}
