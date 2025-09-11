package com.guideon.simulation.result.service;

import com.guideon.simulation.result.dto.PageResponse;
import com.guideon.simulation.result.dto.SimulationResultDetailDto;
import com.guideon.simulation.result.dto.SimulationResultListDto;
import com.guideon.simulation.result.dto.HomeSummaryDto;

public interface SimulationResultService {
    PageResponse<SimulationResultListDto> getList(Long memberId, int page, int size);
    SimulationResultDetailDto getDetail(Long id, Long memberId);

    /** 홈 요약 */
    HomeSummaryDto getHomeSummary(Long memberId);

    void updateSimulationStatus(Long sessionId, double planTotalScore);
}
