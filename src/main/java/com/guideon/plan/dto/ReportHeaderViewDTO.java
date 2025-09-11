package com.guideon.plan.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportHeaderViewDTO {
    private Long reportId;
    private Long memberId;
    private Long fileId;
    private Double totalScore;
    private Integer successProb;
    private String strengthsJson; // JSON 문자열
    private String risksJson;     // JSON 문자열

    // 파일 메타(없으면 제거하세요)
    private String fileName;
    private String uploadedAt;
}
