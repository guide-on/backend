package com.guideon.funds.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 카카오 API 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "카카오 API 주소 검색 요청")
public class KakaoApiRequestDTO {
    
    @ApiModelProperty(value = "검색할 주소", example = "서울시 중구 세종대로 110")
    private String address;
    
    @ApiModelProperty(value = "지원센터 ID (좌표 업데이트용)", example = "1")
    private Long centerId;
}
