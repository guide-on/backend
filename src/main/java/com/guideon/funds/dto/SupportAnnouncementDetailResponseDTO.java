package com.guideon.funds.dto;

import com.guideon.funds.domain.SupportAnnouncement;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.format.DateTimeFormatter;

/**
 * 소상공인 지원사업 공고 상세 조회 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportAnnouncementDetailResponseDTO {
    
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
    private String createdAt;
    
    public SupportAnnouncementDetailResponseDTO(SupportAnnouncement announcement) {
        this.id = announcement.getId();
        this.region = announcement.getRegion();
        this.industry = announcement.getIndustry();
        this.target = announcement.getTarget();
        this.title = announcement.getTitle();
        this.agency = announcement.getAgency();
        this.applyPeriod = announcement.getApplyPeriod();
        this.category = announcement.getCategory();
        this.applyStatus = announcement.getApplyStatus();
        this.noticeTitle = announcement.getNoticeTitle();
        this.recruitType = announcement.getRecruitType();
        this.programType = announcement.getProgramType();
        this.projectPeriod = announcement.getProjectPeriod();
        this.applyDetail = announcement.getApplyDetail();
        this.description = announcement.getDescription();
        this.attachments = announcement.getAttachments();
        this.tags = announcement.getTags();
        this.consultPeriod = announcement.getConsultPeriod();
        
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        this.createdAt = announcement.getCreatedAt() != null ? 
            announcement.getCreatedAt().format(formatter) : null;
    }
}
