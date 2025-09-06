package com.guideon.member.dto;

import com.guideon.member.domain.Gender;
import com.guideon.member.domain.MemberType;
import com.guideon.security.account.domain.MemberVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class MemberJoinDTO {
    private MemberType memberType;
    private String email;      // 로그인 ID
    private String password;   // 평문 비밀번호
    private String name;
    private String phone;
    private Gender gender;     // enum: MALE / FEMALE
    private String birth;      // 예: "2009-10-16"
    private String residenceSggCode;
    private BusinessProfileDTO business;     // 사업자유형
    private PreferenceDTO preference;        // 일반유형 선택(건너뛰기 가능)

    // 서비스에서 호출할 조건부 검증
    public void conditionalValidate() {
        if (memberType == MemberType.SOLE_PROPRIETOR) {
            if (business == null) throw new IllegalArgumentException("사업자 정보가 필요합니다.");
            business.normalize();
            business.validateRequired();

            // 사업자 유형은 관심정보 받지 않음
            if (preference != null
                    && (!preference.dedupRegions().isEmpty() || !preference.dedupIndustries().isEmpty())) {
                throw new IllegalArgumentException("사업자 유형은 관심 지역/업종을 받지 않습니다.");
            }
        }
    }

    public MemberVO toVO(PasswordEncoder encoder) {
        return MemberVO.builder()
                .memberType(memberType)
                .email(email)
                .password(encoder.encode(password))
                .name(name)
                .phone(phone.replace("-", ""))
                .gender(gender)
                .birth(LocalDate.parse(birth))
                .residenceSggCode(residenceSggCode)
                .build();
    }
}
