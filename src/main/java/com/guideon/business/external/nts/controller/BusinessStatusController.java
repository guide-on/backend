package com.guideon.business.external.nts.controller;

import com.guideon.business.external.nts.dto.BizStatusCheckResponse;
import com.guideon.business.external.nts.dto.BusinessStatusItem;
import com.guideon.business.external.nts.dto.BusinessStatusResponse;
import com.guideon.business.external.nts.service.BusinessStatusService;
import io.swagger.annotations.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/biz")
@Api(tags = "사업자 API", description = "사업자 관련 기능 제공")
public class BusinessStatusController {
    private final BusinessStatusService businessStatusService;

    @ApiOperation(value = "사업자등록번호 단건 상태 확인", notes = "사업자등록번호(숫자 10자리)를 받아 계속/휴업/폐업/미등록 여부를 반환합니다.")
    @ApiResponses({
            @ApiResponse(code = 200, message = "정상 응답", response = BizStatusCheckResponse.class),
            @ApiResponse(code = 400, message = "잘못된 요청(숫자 10자리 아님)")
    })
    @GetMapping("/status/check")
    public ResponseEntity<BizStatusCheckResponse> checkBizRegNo(
            @ApiParam(value = "사업자등록번호(숫자 10자리)", required = true, example = "1234567890")
            @RequestParam String bno) {
        // 형식 검증 및 정규화
        String clean = businessStatusService.normalize(bno);
        if (clean == null || clean.length() != 10) {
            return ResponseEntity.badRequest().body(BizStatusCheckResponse.invalidBno(bno));
        }

        // 단건 조회
        BusinessStatusResponse r = businessStatusService.checkStatus(List.of(clean));
        BusinessStatusItem item = (r != null && r.getData() != null && !r.getData().isEmpty())
                ? r.getData().get(0) : null;

        // 미등록
        if (item == null) {
            return ResponseEntity.ok(BizStatusCheckResponse.notRegistered(clean));
        }

        String code = item.getB_stt_cd();
        String label = item.getB_stt();

        // 사업 상태별
        if ("01".equals(code)) {
            return ResponseEntity.ok(BizStatusCheckResponse.ok(clean, code, label));
        } else if ("02".equals(code)) {
            return ResponseEntity.ok(BizStatusCheckResponse.inactive(clean, code, label, "휴업 상태의 사업자입니다."));
        } else if ("03".equals(code)) {
            return ResponseEntity.ok(BizStatusCheckResponse.inactive(clean, code, label, "폐업 상태의 사업자입니다."));
        } else {
            return ResponseEntity.ok(BizStatusCheckResponse.unknown(clean, code, label));
        }
    }
}
