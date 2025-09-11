package com.guideon.member.mapper;

import com.guideon.member.domain.BusinessProfileVO;
import org.apache.ibatis.annotations.Param;

public interface BusinessProfileMapper {
    int existsBizRegNo(@Param("bizRegNo") String bizRegNo);
    int insert(BusinessProfileVO bp);
    BusinessProfileVO getBusinessProfile(Long memberId);
}
