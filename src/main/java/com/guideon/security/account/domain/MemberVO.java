package com.guideon.security.account.domain;

import com.guideon.member.domain.Gender;
import com.guideon.member.domain.MemberType;
import lombok.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberVO {
    private Long memberId;
    private MemberType memberType;
    private String email;
    private String nickname;
    private String password;
    private String name;
    private String phone;
    private Gender gender;
    private LocalDate birth;
    private Date createdAt;
    private Date updatedAt;

    @Builder.Default
    private List<AuthVO> authList = new ArrayList<>();
}
