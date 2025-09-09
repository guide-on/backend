package com.guideon.member.mapper;

import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IndividualPreferenceMapper {
    void insertRegionBatch(@Param("memberId") Long memberId,
                           @Param("codes") List<String> codes);

    void insertIndustryBatch(@Param("memberId") Long memberId,
                             @Param("codes") List<String> codes);
}
