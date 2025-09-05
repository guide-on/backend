package com.guideon.document.mapper;

import com.guideon.document.domain.IndustryRulesVO;
import com.guideon.document.domain.PolicyVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.*;

@Mapper
public interface PolicyMapper {
    /**
     * 활성화된 정책자금 전체 조회
     */
    List<PolicyVO> selectActivePolicies();
}
