package com.guideon.simulation.result.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import com.guideon.simulation.result.domain.SimulationResult;

import java.util.List;

@Mapper
public interface SimulationResultMapper {
    long countByMember(@Param("memberId") Long memberId);
    List<SimulationResult> findPageByMember(@Param("memberId") Long memberId,
                                            @Param("offset") int offset,
                                            @Param("limit") int limit);
    SimulationResult findByIdAndMember(@Param("id") Long id,
                                       @Param("memberId") Long memberId);

    // 신규
    long countInProgressByMember(@Param("memberId") Long memberId);
    /** 최근 COMPLETED 1건의 total_probability_pct */
    Double findLatestCompletedProbability(@Param("memberId") Long memberId);
}
