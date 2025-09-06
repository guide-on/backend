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
 * 지원센터 목록 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ApiModel(description = "지원센터 목록 응답 정보")
public class SupportCenterListResponseDTO {
    private List<SupportCenter> centers;
    private int count;
    
    // 센터 목록으로 생성하는 생성자
    public SupportCenterListResponseDTO(List<SupportCenter> centers) {
        this.centers = centers;
        this.count = centers != null ? centers.size() : 0;
    }
}
