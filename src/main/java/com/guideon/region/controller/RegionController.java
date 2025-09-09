package com.guideon.region.controller;

import com.guideon.region.dto.SidoDTO;
import com.guideon.region.service.RegionService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/region")
@Api(tags = "지역 API", description = "지역 관련 기능 제공")
public class RegionController {
    private final RegionService regionService;

    @ApiOperation("시·도 조회")
    @GetMapping("/sido")
    public ResponseEntity<List<SidoDTO>> getSido() {
        return ResponseEntity.ok()
                .header("Cache-Control","public, max-age=86400")
                .body(regionService.getSidoList());
    }
}
