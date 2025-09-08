package com.guideon.document.mapper;

import com.guideon.document.domain.IndustryRulesVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface IndustryRulesMapper {

    /**
     * 업종코드로 소상공인 조건 조회 (industry_rules 테이블)
     */
    IndustryRulesVO selectIndustryRuleByCode(String industryCode);
}
