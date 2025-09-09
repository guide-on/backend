package com.guideon.industry.code.controller;

import com.guideon.common.pagination.Page;
import com.guideon.common.pagination.PageRequest;
import com.guideon.industry.code.dto.Ksic5DTO;
import com.guideon.industry.code.service.IndustryCodeService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/industry/code")
@Api(tags = "업종코드(KSIC) API", description = "업종코드 관련 기능 제공")
public class IndustryCodeController {

    private final IndustryCodeService service;

    @ApiOperation("KSIC 5자리 코드/이름 페이지 조회")
    @GetMapping("/ksic5")
    public ResponseEntity<Page<Ksic5DTO>> listKsic5(
            @ApiParam(value = "페이지네이션 요청 객체", required = true) PageRequest pageRequest,
            @ApiParam(value="코드 prefix(숫자, 최대 5자리)") @RequestParam(required=false) String code,
            @ApiParam(value="업종명 부분일치")               @RequestParam(required=false) String name
    ) {
        PageRequest pr = PageRequest.of(pageRequest.getPage(), pageRequest.getAmount());
        return ResponseEntity.ok(service.getKsic5CodesPage(pr, code, name));
    }
}