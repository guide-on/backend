package com.guideon.funds.mapper;

import com.guideon.funds.domain.SupportCenter;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * 지원센터 Mapper 인터페이스
 */
@Mapper
public interface SupportCenterMapper {
    
    /**
     * 전체 지원센터 목록 조회
     * @return 전체 지원센터 목록
     */
    List<SupportCenter> selectAllCenters();
    
    /**
     * 좌표가 있는 지원센터만 조회
     * @return 좌표가 있는 지원센터 목록
     */
    List<SupportCenter> selectCentersWithCoordinates();
    
    /**
     * 특정 관할구역의 지원센터 조회
     * @param jurisdiction 관할구역
     * @return 해당 관할구역의 지원센터 목록
     */
    List<SupportCenter> selectCentersByJurisdiction(String jurisdiction);
    
    /**
     * ID로 지원센터 단건 조회
     * @param id 지원센터 ID
     * @return 지원센터 정보
     */
    SupportCenter selectCenterById(Long id);
    
    /**
     * 지원센터 좌표 업데이트
     * @param supportCenter 업데이트할 지원센터 정보 (id, lat, lng 필수)
     * @return 업데이트된 행 수
     */
    int updateCenterCoordinates(SupportCenter supportCenter);
}
