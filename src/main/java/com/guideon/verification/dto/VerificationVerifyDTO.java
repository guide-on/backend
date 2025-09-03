package com.guideon.verification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "인증번호 검증 객체")
public class VerificationVerifyDTO {
    @ApiModelProperty(value = "연락처(email 또는 phone)", example = "adimn@example.com")
    private String target;
    @ApiModelProperty(value = "인증번호", example = "123456")
    private String code;
    @ApiModelProperty(value = "인증수단 - true: 이메일, false: 전화번호", example = "true")
    private Boolean isEmail;
}
