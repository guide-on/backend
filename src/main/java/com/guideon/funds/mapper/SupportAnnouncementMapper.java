package com.guideon.funds.mapper;

import com.guideon.funds.domain.SupportAnnouncement;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 소상공인 지원사업 공고 매퍼 인터페이스
 */
@Mapper
public interface SupportAnnouncementMapper {
    
    /**
     * 전체 소상공인 지원사업 공고 목록 조회
     * @return 전체 공고 목록
     */
    List<SupportAnnouncement> selectAllAnnouncements();
    
    /**
     * 특정 ID로 소상공인 지원사업 공고 상세 조회
     * @param id 공고 ID
     * @return 공고 상세 정보
     */
    SupportAnnouncement selectAnnouncementById(@Param("id") Long id);
    
    /**
     * 지역별 소상공인 지원사업 공고 목록 조회
     * @param region 지역명
     * @return 지역별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByRegion(@Param("region") String region);
    
    /**
     * 업종별 소상공인 지원사업 공고 목록 조회
     * @param industry 업종명
     * @return 업종별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByIndustry(@Param("industry") String industry);
    
    /**
     * 지역 + 업종별 소상공인 지원사업 공고 목록 조회
     * @param region 지역명
     * @param industry 업종명
     * @return 지역+업종별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByRegionAndIndustry(@Param("region") String region, @Param("industry") String industry);
    
    /**
     * 신청 상태별 소상공인 지원사업 공고 목록 조회
     * @param applyStatus 신청상태 (신청가능, 마감 등)
     * @return 신청상태별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByApplyStatus(@Param("applyStatus") String applyStatus);
    
    /**
     * 키워드로 소상공인 지원사업 공고 검색 (제목, 공고명, 내용에서 검색)
     * @param keyword 검색 키워드
     * @return 검색된 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByKeyword(@Param("keyword") String keyword);
    
    /**
     * 기관별 소상공인 지원사업 공고 목록 조회
     * @param agency 기관명
     * @return 기관별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByAgency(@Param("agency") String agency);
    
    /**
     * 카테고리별 소상공인 지원사업 공고 목록 조회
     * @param category 카테고리
     * @return 카테고리별 공고 목록
     */
    List<SupportAnnouncement> selectAnnouncementsByCategory(@Param("category") String category);
}
