package com.guideon.ocr.controller;

import com.guideon.ocr.dto.BizRegOcrResultDTO;
import com.guideon.ocr.service.BizRegOcrService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ocr")
public class BizRegOcrController {

    private final BizRegOcrService service;

    @ApiOperation("사업자등록증 OCR(이미지 업로드)")
    @PostMapping(value="/bizreg", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<BizRegOcrResultDTO> ocrBizReg(
            @ApiParam(value="사업자등록증 이미지(jpg/png/jpeg/webp/bmp/tiff)", required = true)
            @RequestPart("file") MultipartFile file
    ) {
        BizRegOcrResultDTO result = service.extract(file);
        // ※ 여기서 원본 이미지는 저장하지 않음(메모리 상에서만 사용)
        return ResponseEntity.ok(result);
    }
}
