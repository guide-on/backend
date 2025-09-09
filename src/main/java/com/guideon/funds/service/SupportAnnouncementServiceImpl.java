package com.guideon.funds.service;

import com.guideon.funds.domain.SupportAnnouncement;
import com.guideon.funds.mapper.SupportAnnouncementMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 소상공인 지원사업 공고 서비스 구현체
 */
@Service
public class SupportAnnouncementServiceImpl implements SupportAnnouncementService {
    
    @Autowired
    private SupportAnnouncementMapper supportAnnouncementMapper;
    
    @Override
    public List<SupportAnnouncement> getAllAnnouncements() {
        return supportAnnouncementMapper.selectAllAnnouncements();
    }
    
    @Override
    public SupportAnnouncement getAnnouncementById(Long id) {
        return supportAnnouncementMapper.selectAnnouncementById(id);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByRegion(String region) {
        return supportAnnouncementMapper.selectAnnouncementsByRegion(region);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByIndustry(String industry) {
        return supportAnnouncementMapper.selectAnnouncementsByIndustry(industry);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByRegionAndIndustry(String region, String industry) {
        return supportAnnouncementMapper.selectAnnouncementsByRegionAndIndustry(region, industry);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByApplyStatus(String applyStatus) {
        return supportAnnouncementMapper.selectAnnouncementsByApplyStatus(applyStatus);
    }
    
    @Override
    public List<SupportAnnouncement> searchAnnouncements(String keyword) {
        return supportAnnouncementMapper.selectAnnouncementsByKeyword(keyword);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByAgency(String agency) {
        return supportAnnouncementMapper.selectAnnouncementsByAgency(agency);
    }
    
    @Override
    public List<SupportAnnouncement> getAnnouncementsByCategory(String category) {
        return supportAnnouncementMapper.selectAnnouncementsByCategory(category);
    }
}
