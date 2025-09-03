package com.guideon.security.account.dto;

import com.guideon.member.domain.MemberType;
import com.guideon.security.account.domain.AuthVO;
import com.guideon.security.account.domain.MemberVO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ApiModel(description = "사용자 정보 응답 객체")
public class UserInfoDTO {

    @ApiModelProperty(value = "사용자 이름", example = "홍길동")
    private String name;

    @ApiModelProperty(value = "사용자 유형", example = "INDIVIDUAL")
    private MemberType memberType;

    @ApiModelProperty(value = "이메일", example = "user@example.com")
    private String email;

    @ApiModelProperty(value = "권한 목록", example = "[\"ROLE_USER\"]")
    private List<String> roles;

    public static UserInfoDTO of(MemberVO member) {
        return new UserInfoDTO(
                member.getName(),
                member.getMemberType(),
                member.getEmail(),
                member.getAuthList().stream()
                        .map(AuthVO::getAuth)
                        .toList()
        );
    }
}

