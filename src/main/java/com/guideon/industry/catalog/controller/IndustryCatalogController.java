package com.guideon.industry.catalog.controller;

import com.guideon.industry.catalog.domain.IndustryTag;
import com.guideon.industry.catalog.infra.IndustryTagCatalog;
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
@RequestMapping("/api/industry/catalog")
@Api(tags = "업종태그 API", description = "업종태그(사용자에게 친숙한 업종명) 관련 기능 제공")
public class IndustryCatalogController {
    private final IndustryTagCatalog catalog;

    @ApiOperation("업종태그 조회")
    @GetMapping("/tags")
    public ResponseEntity<List<IndustryTag>> tags() {
        return ResponseEntity.ok()
                .header("Cache-Control", "public,max-age=86400")
                .body(catalog.findAll());
    }
}
