package com.guideon.ocr.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BizRegOcrResultDTO {
    private String rawText;          // 전체 OCR 원문 (선택 저장)
    private String bizRegNo;         // 사업자등록번호 (10자리)
    private String companyName;      // 상호/법인명
    private String ownerName;        // 대표자
    private String bizType;          // 업태
    private String bizItems;         // 종목(여러개면 쉼표)
    private String address;          // 사업장 소재지(한 줄)
    private String openedOn;         // 개업일(YYYY-MM-DD)
    private Float  ocrConfidence;    // 전체 신뢰도(대략)
}
