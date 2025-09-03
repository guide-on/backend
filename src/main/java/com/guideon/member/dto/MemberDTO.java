package com.guideon.member.dto;

import com.guideon.member.domain.Gender;
import com.guideon.member.domain.MemberType;
import com.guideon.security.account.domain.AuthVO;
import com.guideon.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberDTO {
    private MemberType memberType;
    private String email;
    private String nickname;
    private String phone;
    private String name;
    private Gender gender;
    private String birth;
    private Date createdAt;            // 등록일
    private Date updatedAt;            // 수정일
    private List<String> authList;     // 권한 목록 (join 처리 필요)

    // MemberVO에서 DTO 생성 (정적 팩토리 메서드)
    public static MemberDTO of(MemberVO m) {
        return MemberDTO.builder()
                .memberType(m.getMemberType())
                .email(m.getEmail())
                .nickname(m.getNickname())
                .phone(m.getPhone())
                .name(m.getName())
                .gender(m.getGender())
                .birth(String.valueOf(m.getBirth()))
                .createdAt(m.getCreatedAt())
                .updatedAt(m.getUpdatedAt())
                .authList(m.getAuthList().stream()
                        .map(AuthVO::getAuth)
                        .toList())
                .build();
    }
}
