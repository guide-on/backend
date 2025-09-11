package com.guideon.simulation.result.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.guideon.simulation.result.domain.SimulationResult;
import com.guideon.simulation.result.dto.PageResponse;
import com.guideon.simulation.result.dto.SimulationResultDetailDto;
import com.guideon.simulation.result.dto.SimulationResultListDto;
import com.guideon.simulation.result.exception.NotFoundException;
import com.guideon.simulation.result.mapper.SimulationResultMapper;
import com.guideon.simulation.result.service.SimulationResultService;
import com.guideon.simulation.result.dto.HomeSummaryDto;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SimulationResultServiceImpl implements SimulationResultService {

    private final SimulationResultMapper mapper;

    @Override
    public PageResponse<SimulationResultListDto> getList(Long memberId, int page, int size) {
        int p = Math.max(0, page);
        int s = Math.max(1, size);
        int offset = p * s;

        long total = mapper.countByMember(memberId);
        List<SimulationResult> rows = mapper.findPageByMember(memberId, offset, s);

        List<SimulationResultListDto> content = rows.stream().map(r -> {
            SimulationResultListDto dto = new SimulationResultListDto();
            dto.setId(r.getSessionId());
            dto.setStartedAt(r.getStartedAt());
            dto.setFundName(r.getFundName());
            dto.setCurrentStep(safeEnum(r.getCurrentStep()));
            dto.setOverallStatus(safeEnum(r.getOverallStatus()));
            dto.setTotalProbabilityPct(r.getTotalProbabilityPct());
            dto.setDocScorePct(r.getDocScorePct());
            dto.setCreditScorePct(r.getCreditScorePct());
            dto.setPlanScorePct(r.getPlanScorePct());
            return dto;
        }).collect(Collectors.toList());

        return new PageResponse<>(content, p, s, total);
    }

    @Override
    public SimulationResultDetailDto getDetail(Long id, Long memberId) {
        SimulationResult r = mapper.findBySessionIdAndMember(id, memberId);
        if (r == null) throw new NotFoundException("시뮬레이션 결과를 찾을 수 없습니다.");

        SimulationResultDetailDto dto = new SimulationResultDetailDto();
        dto.setId(r.getSessionId());
        dto.setMemberId(r.getMemberId());
        dto.setFundName(r.getFundName());
        dto.setCurrentStep(safeEnum(r.getCurrentStep()));
        dto.setOverallStatus(safeEnum(r.getOverallStatus()));
        dto.setStartedAt(r.getStartedAt());
        dto.setUpdatedAt(r.getUpdatedAt());

        dto.setDocSessionStatus(safeEnum(r.getDocSessionStatus()));

        dto.setTotalCreditScore(r.getTotalCreditScore());
        dto.setHybridCreditScore(r.getHybridCreditScore());
        dto.setTraditionalCreditScore(r.getTraditionalCreditScore());
        dto.setCreditLastUpdated(r.getCreditLastUpdated());

        dto.setPlanTotalScore(r.getPlanTotalScore());

        dto.setDocScorePct(r.getDocScorePct());
        dto.setCreditScorePct(r.getCreditScorePct());
        dto.setPlanScorePct(r.getPlanScorePct());
        dto.setTotalProbabilityPct(r.getTotalProbabilityPct());
        return dto;
    }

    private static String safeEnum(Enum<?> e) {
        return e == null ? null : e.name();
    }

    @Override
    public HomeSummaryDto getHomeSummary(Long memberId) {
        long joined = mapper.countByMember(memberId);
        long inProgress = mapper.countInProgressByMember(memberId);
        // 최근 완료건의 total_probability_pct(소수) -> 반올림 정수
        Double recent = mapper.findLatestCompletedProbability(memberId);
        Integer recentRounded = (recent == null) ? null : (int)Math.round(recent);
        return new HomeSummaryDto(joined, inProgress, recentRounded);
    }
}
