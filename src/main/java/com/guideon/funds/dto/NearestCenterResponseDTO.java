package com.guideon.funds.dto;

import com.guideon.funds.domain.SupportCenter;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 가장 가까운 센터 찾기 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "가장 가까운 센터 찾기 응답")
public class NearestCenterResponseDTO {
    
    @ApiModelProperty(value = "검색된 사업장 주소", example = "서울시 강남구 역삼동 123-45")
    private String searchedAddress;
    
    @ApiModelProperty(value = "사업장 위도", example = "37.5665")
    private Double businessLat;
    
    @ApiModelProperty(value = "사업장 경도", example = "126.9780")
    private Double businessLng;
    
    @ApiModelProperty(value = "가까운 센터 목록")
    private List<NearestCenter> nearestCenters;
    
    @ApiModelProperty(value = "검색 성공 여부", example = "true")
    private boolean success;
    
    @ApiModelProperty(value = "오류 메시지", example = "주소를 찾을 수 없습니다")
    private String errorMessage;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @ApiModel(description = "가까운 센터 정보")
    public static class NearestCenter {
        
        @ApiModelProperty(value = "센터 정보")
        private SupportCenter center;
        
        @ApiModelProperty(value = "거리 (km)", example = "12.34")
        private Double distanceKm;
        
        @ApiModelProperty(value = "순위", example = "1")
        private Integer rank;
    }
}
