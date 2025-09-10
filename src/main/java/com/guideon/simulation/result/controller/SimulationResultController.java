package com.guideon.simulation.result.controller;

import org.springframework.web.bind.annotation.*;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.common.exception.UnauthorizedException;
import com.guideon.security.util.LoginUserProvider;
import com.guideon.simulation.result.dto.PageResponse;
import com.guideon.simulation.result.dto.SimulationResultDetailDto;
import com.guideon.simulation.result.dto.SimulationResultListDto;
import com.guideon.simulation.result.service.SimulationResultService;

import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/simulation/results")
@Api(tags = "승인 시뮬레이션 결과 API", description = "시뮬레이션 결과 목록/상세 조회")
public class SimulationResultController {

    private final SimulationResultService service;
    private final LoginUserProvider loginUserProvider;

    /** 로그인 사용자 ID 없으면 401 */
    private Long currentMemberIdOrThrow() {
        Long memberId = loginUserProvider.getLoginMemberId();
        if (memberId == null) throw new UnauthorizedException("로그인이 필요합니다.");
        return memberId;
    }

    // -----------------------------
    // 결과 목록
    // -----------------------------
    @GetMapping
    @ApiOperation(value = "시뮬레이션 결과 목록 조회",
            notes = "로그인 사용자의 시뮬레이션 결과를 페이지네이션으로 조회합니다. 최신 업데이트 순으로 반환됩니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "Cookie",
                    value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)",
                    required = true, paramType = "header", dataType = "string"),
            @ApiImplicitParam(name = "page",
                    value = "페이지 번호(0-base)", defaultValue = "0",
                    required = false, paramType = "query", dataType = "int"),
            @ApiImplicitParam(name = "size",
                    value = "페이지 크기", defaultValue = "20",
                    required = false, paramType = "query", dataType = "int")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "인증 실패(로그인 필요)")
    })
    public CommonResponseDTO<PageResponse<SimulationResultListDto>> list(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Long memberId = currentMemberIdOrThrow();
        log.debug("[RESULT-LIST] memberId={}, page={}, size={}", memberId, page, size);
        return CommonResponseDTO.ok(service.getList(memberId, page, size));
    }

    // -----------------------------
    // 결과 상세
    // -----------------------------
    @GetMapping("/{id}")
    @ApiOperation(value = "시뮬레이션 결과 상세 조회",
            notes = "id에 해당하는 시뮬레이션 결과의 상세 정보를 조회합니다. 본인 데이터만 접근 가능합니다.")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "id", value = "시뮬레이션 결과 ID",
                    required = true, paramType = "path", dataType = "long"),
            @ApiImplicitParam(name = "Cookie",
                    value = "로그인 쿠키 (예: ACCESS_TOKEN=eyJ...)", required = true,
                    paramType = "header", dataType = "string")
    })
    @ApiResponses({
            @ApiResponse(code = 200, message = "조회 성공"),
            @ApiResponse(code = 401, message = "인증 실패(로그인 필요)"),
            @ApiResponse(code = 404, message = "대상을 찾을 수 없음")
    })
    public CommonResponseDTO<SimulationResultDetailDto> detail(
            @PathVariable Long id
    ) {
        Long memberId = currentMemberIdOrThrow();
        log.debug("[RESULT-DETAIL] memberId={}, id={}", memberId, id);
        return CommonResponseDTO.ok(service.getDetail(id, memberId));
    }
}
