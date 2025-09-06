package com.guideon.funds.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 좌표 업데이트 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "좌표 업데이트 응답")
public class CoordinateUpdateResponseDTO {
    
    @ApiModelProperty(value = "지원센터 ID", example = "1")
    private Long centerId;
    
    @ApiModelProperty(value = "지원센터명", example = "서울지원센터")
    private String centerName;
    
    @ApiModelProperty(value = "검색된 주소", example = "서울 중구 세종대로 110")
    private String foundAddress;
    
    @ApiModelProperty(value = "업데이트된 위도", example = "37.56673456781234")
    private Double lat;
    
    @ApiModelProperty(value = "업데이트된 경도", example = "126.97794838463647")
    private Double lng;
    
    @ApiModelProperty(value = "업데이트 성공 여부", example = "true")
    private boolean updated;
    
    @ApiModelProperty(value = "오류 메시지", example = "주소를 찾을 수 없습니다")
    private String errorMessage;
}
