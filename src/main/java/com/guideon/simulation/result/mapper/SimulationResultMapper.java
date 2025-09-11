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

    SimulationResult findBySessionIdAndMember(@Param("sessionId") Long sessionId,
                                              @Param("memberId") Long memberId);

    long countInProgressByMember(@Param("memberId") Long memberId);

    /** 최근 COMPLETED 1건의 total_probability_pct */
    Double findLatestCompletedProbability(@Param("memberId") Long memberId);

    void updatePlanResult(@Param("sessionId") Long sessionId, @Param("planTotalScore") double planTotalScore);
}