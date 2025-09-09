package com.guideon.funds.service;

import com.guideon.funds.domain.SupportCenter;
import com.guideon.funds.mapper.SupportCenterMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 지원센터 서비스
 */
@Service
public class SupportCenterService {

    
    @Autowired
    private SupportCenterMapper supportCenterMapper;
    
    /**
     * 전체 지원센터 목록 조회
     * @return 전체 지원센터 목록
     */
    public List<SupportCenter> getAllCenters() {
        
        List<SupportCenter> centers = supportCenterMapper.selectAllCenters();

        return centers;
    }
    
    /**
     * 좌표가 있는 지원센터만 조회
     * @return 좌표가 있는 지원센터 목록
     */
    public List<SupportCenter> getCentersWithCoordinates() {

        List<SupportCenter> centers = supportCenterMapper.selectCentersWithCoordinates();
        
        return centers;
    }
    
    /**
     * ID로 지원센터 단건 조회
     * @param id 지원센터 ID
     * @return 지원센터 정보
     */
    public SupportCenter getCenterById(Long id) {
        return supportCenterMapper.selectCenterById(id);
    }
    
    /**
     * 특정 관할구역의 지원센터 조회
     * @param jurisdiction 관할구역
     * @return 해당 관할구역의 지원센터 목록
     */
    public List<SupportCenter> getCentersByJurisdiction(String jurisdiction) {
        return supportCenterMapper.selectCentersByJurisdiction(jurisdiction);
    }
    
    /**
     * 지원센터 좌표 업데이트
     * @param supportCenter 업데이트할 지원센터 정보 (id, lat, lng 필수)
     * @return 업데이트 성공 여부
     */
    public boolean updateCenterCoordinates(SupportCenter supportCenter) {
        int updatedCount = supportCenterMapper.updateCenterCoordinates(supportCenter);
        
        boolean success = updatedCount > 0;
        
        return success;
    }
}
