package com.guideon.simulation.result.service;

import com.guideon.simulation.result.dto.PageResponse;
import com.guideon.simulation.result.dto.SimulationResultDetailDto;
import com.guideon.simulation.result.dto.SimulationResultListDto;

public interface SimulationResultService {
    PageResponse<SimulationResultListDto> getList(Long memberId, int page, int size);
    SimulationResultDetailDto getDetail(Long id, Long memberId);
}
