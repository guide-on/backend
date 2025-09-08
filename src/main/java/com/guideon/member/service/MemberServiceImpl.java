package com.guideon.member.service;

import com.guideon.common.redis.RedisKeyUtil;
import com.guideon.common.redis.RedisService;
import com.guideon.common.util.PasswordValidator;
import com.guideon.industry.catalog.infra.IndustryTagCatalog;
import com.guideon.member.domain.BusinessProfileVO;
import com.guideon.member.domain.MemberType;
import com.guideon.member.dto.BusinessProfileDTO;
import com.guideon.member.dto.MemberDTO;
import com.guideon.member.dto.MemberJoinDTO;
import com.guideon.member.dto.PreferenceDTO;
import com.guideon.member.mapper.BusinessProfileMapper;
import com.guideon.member.mapper.IndividualPreferenceMapper;
import com.guideon.member.mapper.MemberMapper;
import com.guideon.security.account.domain.AuthVO;
import com.guideon.security.account.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper memberMapper;
    private final BusinessProfileMapper businessProfileMapper;
    private final IndividualPreferenceMapper preferenceMapper;
    private final IndustryTagCatalog industryTagCatalog;
    private final PasswordEncoder passwordEncoder;
    private final RedisService redisService;

    // 회원 정보 조회
    @Override
    public MemberDTO get(Long id, String email) {
        MemberVO member = getMember(id, email);
        return MemberDTO.of(member);
    }

    private MemberVO getMember(Long id, String email) {
        return Optional.ofNullable(memberMapper.get(id, email))
                .orElseThrow(NoSuchElementException::new);
    }

    // 회원 가입(선언적 트랜잭션 처리)
    @Transactional
    @Override
    public MemberDTO join(MemberJoinDTO dto) {
        String email = dto.getEmail();
        String phoneNum = dto.getPhone().replace("-", "");
        // 이메일 & 전화번호 인증, 비밀번호 검증 확인
        validateVerification(email, phoneNum);
        if (!PasswordValidator.isValid(dto.getPassword())) {
            throw new IllegalArgumentException("비밀번호 형식이 유효하지 않습니다.");
        }
        // 회원 유형별 추가 검증
        dto.conditionalValidate();

        MemberVO member = registerMember(dto.toVO(passwordEncoder));

        // 부가 정보 저장 (분리된 메서드 호출)
        if (dto.getMemberType() == MemberType.SOLE_PROPRIETOR) {
            saveBusinessProfile(member.getMemberId(), dto.getBusiness());
        } else if (dto.getMemberType() == MemberType.GENERAL) {
            saveIndividualPreferences(member.getMemberId(), dto.getPreference());
        }

        // 인증상태 삭제 (가입 성공 여부와 상관없이 1회용 인증)
        redisService.delete(RedisKeyUtil.verified(phoneNum));
        redisService.delete(RedisKeyUtil.verified(email));

        // 저장된 회원정보 반환
        return MemberDTO.of(member);
    }

    /** 사업자 회원의 사업자 정보 저장 */
    private void saveBusinessProfile(Long memberId, BusinessProfileDTO businessDTO) {
        if (businessDTO == null) {
            throw new IllegalArgumentException("사업자 정보가 필요합니다.");
        }
        BusinessProfileVO business = businessDTO.toVO(memberId);
        businessProfileMapper.insert(business);
    }

    /** 일반 회원의 관심 지역/업종 저장 (건너뛰기 허용) */
    private void saveIndividualPreferences(Long memberId, PreferenceDTO pref) {
        if (pref == null) return;

        // 지역: 시군구 5자리만 저장
        List<String> regionCodes = pref.dedupRegions();
        if (!regionCodes.isEmpty()) {
            preferenceMapper.insertRegionBatch(memberId, regionCodes);
        }

        // 업종: 2자리 KSIC만 저장
        List<String> industryCodes = pref.dedupIndustries();
        if (industryCodes.isEmpty()) {
            List<String> tags = pref.safeTags();
            if (!tags.isEmpty()) {
                industryCodes = tags.stream()
                        .flatMap(tag -> industryTagCatalog.codesOf(tag).stream())
                        .filter(code -> code != null && code.matches("^\\d{2}$"))
                        .distinct()
                        .collect(Collectors.toList());
            }
        }

        if (!industryCodes.isEmpty()) {
            preferenceMapper.insertIndustryBatch(memberId, industryCodes);
        }
    }

    // 회원 등록
    private MemberVO registerMember(MemberVO member) {
        memberMapper.insert(member);

        // 권한정보 저장
        AuthVO auth = new AuthVO(member.getMemberId(), "ROLE_MEMBER");
        memberMapper.insertAuth(auth);

        // 저장된 회원정보 반환
        return getMember(member.getMemberId(), null);
    }

    // 이메일 & 전화번호 인증 확인
    private void validateVerification(String email, String phone) {
        if (!redisService.isVerified(email)) {
            throw new IllegalStateException("이메일 인증을 먼저 완료하세요.");
        }
        if (!redisService.isVerified(phone)) {
            throw new IllegalStateException("전화번호 인증을 먼저 완료하세요.");
        }
        if (memberMapper.existsPhone(phone) > 0) {
            throw new IllegalArgumentException("이미 가입된 전화번호입니다.");
            //이미 가입된 계정이 있습니다. 로그인 화면으로 이동합니다.
        }
    }

    // 이메일(아이디) 중복 여부
    @Override
    public boolean existsByEmail(String email) {
        return memberMapper.existsEmail(email) > 0;
    }

    @Override
    public String getPhoneByEmail(String email) {
        String phone = memberMapper.findPhoneByEmail(email);
        if (phone == null) {
            throw new IllegalArgumentException("해당 이메일로 가입된 회원이 없습니다.");
        }
        return phone;
    }

    @Override
    public String getEmailById(Long id) {
        String email = memberMapper.findEmailById(id);
        if (email == null) {
            throw new IllegalArgumentException("존재하지 않는 회원입니다.");
        }
        return email;
    }
}
