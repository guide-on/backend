package com.guideon.verification.dto;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "인증번호 발송 객체")
public class VerificationSendDTO {
    @ApiModelProperty(value = "연락처(email 또는 phone)", example = "adimn@example.com")
    private String target;                 // 이메일 또는 전화번호
    @ApiModelProperty(value = "인증수단 - true: 이메일, false: 전화번호", example = "true")
    private Boolean isEmail;               // EMAIL or PHONE
    @ApiModelProperty(value = "true: 회원가입, false: 비밀번호 재설정", example = "true")
    private Boolean isSignup;             // true: 회원가입, false: 비밀번호 재설정
}
