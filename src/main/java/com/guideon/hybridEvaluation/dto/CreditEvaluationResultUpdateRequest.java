package com.guideon.hybridEvaluation.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "신용평가 결과 수정 요청")
public class CreditEvaluationResultUpdateRequest {
    
    @ApiModelProperty(value = "사업체 고유 식별자", required = true, example = "1001")
    private Long userId;
    
    @ApiModelProperty(value = "우리 서비스 최종 신용점수", example = "750")
    private Integer totalScore;
    
    @ApiModelProperty(value = "상환이력 항목 점수", example = "150")
    private Integer repaymentHistoryScore;
    
    @ApiModelProperty(value = "부채수준 항목 점수", example = "120")
    private Integer debtLevelScore;
    
    @ApiModelProperty(value = "신용거래기간 항목 점수", example = "180")
    private Integer creditPeriodScore;
    
    @ApiModelProperty(value = "신용형태 항목 점수", example = "160")
    private Integer creditPatternScore;
    
    @ApiModelProperty(value = "비금융 항목 점수", example = "140")
    private Integer nonFinancialScore;
}