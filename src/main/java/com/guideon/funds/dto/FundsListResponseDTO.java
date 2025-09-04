package com.guideon.funds.dto;

import com.guideon.funds.domain.Funds;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 정책지원금 목록 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FundsListResponseDTO {
    private Long id;
    private Integer year;
    private String categoryCode;
    private String name;
    private String status;
    private String target;
    private String rate;
    private String term;
    private String limitAmount;
    private LocalDateTime createdAt;
    private boolean isSaved; // 북마크 여부
    
    public static FundsListResponseDTO from(Funds funds) {
        return FundsListResponseDTO.builder()
                .id(funds.getId())
                .year(funds.getYear())
                .categoryCode(funds.getCategoryCode())
                .name(funds.getName())
                .status(funds.getStatus())
                .target(funds.getTarget())
                .rate(funds.getRate())
                .term(funds.getTerm())
                .limitAmount(funds.getLimitAmount())
                .createdAt(funds.getCreatedAt())
                .isSaved(false)
                .build();
    }
}
