package com.guideon.simulation.result.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class HomeSummaryDto {
    /** 지금까지 참여한 전체 개수 */
    private long joinedCount;
    /** 현재 진행중(IN_PROGRESS) 개수 */
    private long inProgressCount;
    /** 최근 완료(COMPLETED) 시뮬의 최종 승인 확률(%) 정수 반올림, 없으면 null */
    private Integer recentCompletedProbabilityPct;
}
