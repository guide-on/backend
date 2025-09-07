package com.guideon.funds.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.funds.dto.FundsDetailResponseDTO;
import com.guideon.funds.dto.FundsListResponseDTO;
import com.guideon.funds.dto.SavedFundsResponseDTO;
import com.guideon.funds.service.FundsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 정책지원금 REST API 컨트롤러
 */
@Log4j2
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Funds", description = "정책지원금 API")
public class FundsController {

    private final FundsService fundsService;

    @GetMapping("/funds")
    @Operation(summary = "정책지원금 전체 목록 조회", description = "모든 정책지원금을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<FundsListResponseDTO>>> getAllFunds() {
        List<FundsListResponseDTO> fundsList = fundsService.getAllFunds();
        return ResponseEntity.ok(CommonResponseDTO.success("정책지원금 목록을 성공적으로 조회했습니다.", fundsList));
    }

    @GetMapping("/funds/{fundsId}")
    @Operation(summary = "정책지원금 상세 조회", description = "특정 정책지원금의 상세 정보를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<FundsDetailResponseDTO>> getFundsDetail(
            @Parameter(description = "정책지원금 ID", required = true)
            @PathVariable Long fundsId
    ) {
        FundsDetailResponseDTO fundsDetail = fundsService.getFundsDetail(fundsId);
        return ResponseEntity.ok(CommonResponseDTO.success("정책지원금 상세 정보를 성공적으로 조회했습니다.", fundsDetail));
    }
    @GetMapping("/funds/search")
    @Operation(summary = "정책지원금 이름 검색", description = "정책지원금 이름으로 검색합니다.")
    public ResponseEntity<CommonResponseDTO<List<FundsListResponseDTO>>> searchFunds(
            @Parameter(description = "검색 키워드", required = true)
            @RequestParam String keyword
    ) {
        log.info("GET /api/funds/search - keyword: {}", keyword);

        List<FundsListResponseDTO> fundsList = fundsService.searchFundsByName(keyword);
        return ResponseEntity.ok(CommonResponseDTO.success("정책지원금 검색을 성공적으로 완료했습니다.", fundsList));
    }

    @PostMapping("/saved-funds/{fundsId}")
    @Operation(summary = "정책지원금 북마크", description = "정책지원금을 북마크에 추가합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> saveFunds(
            @Parameter(description = "정책지원금 ID", required = true)
            @PathVariable Long fundsId
    ) {
        fundsService.saveFunds(fundsId);
        return ResponseEntity.ok(CommonResponseDTO.success("정책지원금이 북마크에 추가되었습니다."));
    }

    @DeleteMapping("/saved-funds/{fundsId}")
    @Operation(summary = "정책지원금 북마크 해제", description = "정책지원금을 북마크에서 제거합니다.")
    public ResponseEntity<CommonResponseDTO<Void>> unsaveFunds(
            @Parameter(description = "정책지원금 ID", required = true)
            @PathVariable Long fundsId
    ) {
        fundsService.unsaveFunds(fundsId);
        return ResponseEntity.ok(CommonResponseDTO.success("정책지원금 북마크가 해제되었습니다."));
    }

    @GetMapping("/saved-funds")
    @Operation(summary = "북마크한 정책지원금 목록 조회", description = "사용자가 북마크한 정책지원금 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<List<SavedFundsResponseDTO>>> getSavedFundsList() {
        List<SavedFundsResponseDTO> savedFundsList = fundsService.getSavedFundsList();
        return ResponseEntity.ok(CommonResponseDTO.success("북마크한 정책지원금 목록을 성공적으로 조회했습니다.", savedFundsList));
    }
}