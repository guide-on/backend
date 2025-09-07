package com.guideon.funds.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 가장 가까운 센터 찾기 요청 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "가장 가까운 센터 찾기 요청")
public class NearestCenterRequestDTO {
    
    @ApiModelProperty(value = "사업장 주소", example = "서울시 강남구 역삼동 123-45", required = true)
    private String businessAddress;
    
    @ApiModelProperty(value = "반환할 센터 개수", example = "3", notes = "기본값: 1개")
    private Integer limit;
}
