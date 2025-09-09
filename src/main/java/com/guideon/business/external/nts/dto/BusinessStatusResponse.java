package com.guideon.business.external.nts.dto;

import lombok.Data;

import java.util.List;

/**
 * 전체 응답
 * status_code: "OK" | "BAD_JSON_REQUEST" | "REQUEST_DATA_MALFORMED" | "TOO_LARGE_REQUEST" | "INTERNAL_ERROR" | "HTTP_ERROR"
 */
@Data
public class BusinessStatusResponse {
    private String status_code;     // 호출 결과 코드
    private Integer match_cnt;      // 일치 건수
    private Integer request_cnt;    // 요청 건수
    private List<BusinessStatusItem> data;
}
