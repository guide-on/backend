package com.guideon.funds.dto;

import com.guideon.funds.domain.SavedFunds;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * 북마크한 정책지원금 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedFundsResponseDTO {
    private Long savedFundsId;
    private Long id;                // support_fund.id
    private Integer year;
    private String categoryCode;
    private String name;
    private String status;
    private String target;
    private String rate;
    private String term;
    private String limitAmount;
    private LocalDateTime savedAt;
    
    public static SavedFundsResponseDTO from(SavedFunds savedFunds) {
        return SavedFundsResponseDTO.builder()
                .savedFundsId(savedFunds.getSavedFundsId())
                .id(savedFunds.getFunds().getId())
                .year(savedFunds.getFunds().getYear())
                .categoryCode(savedFunds.getFunds().getCategoryCode())
                .name(savedFunds.getFunds().getName())
                .status(savedFunds.getFunds().getStatus())
                .target(savedFunds.getFunds().getTarget())
                .rate(savedFunds.getFunds().getRate())
                .term(savedFunds.getFunds().getTerm())
                .limitAmount(savedFunds.getFunds().getLimitAmount())
                .savedAt(savedFunds.getCreatedAt())
                .build();
    }
}
