package com.guideon.funds.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

/**
 * 소상공인 지원사업 공고 도메인 객체
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportAnnouncement {
    
    private Long id;
    private String region;
    private String industry;
    private String target;
    private String title;
    private String agency;
    private String applyPeriod;
    private String category;
    private String applyStatus;
    private String noticeTitle;
    private String recruitType;
    private String programType;
    private String projectPeriod;
    private String applyDetail;
    private String description;
    private String attachments;
    private String tags;
    private String consultPeriod;
    private LocalDateTime createdAt;
}
