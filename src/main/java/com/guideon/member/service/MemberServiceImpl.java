package com.guideon.member.service;

import com.guideon.member.dto.MemberDTO;
import com.guideon.member.mapper.MemberMapper;
import com.guideon.security.account.domain.MemberVO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
    private final MemberMapper mapper;

    // 회원 정보 조회
    @Override
    public MemberDTO get(Long id, String email) {
        MemberVO member = Optional.ofNullable(mapper.get(id, email))
                .orElseThrow(NoSuchElementException::new);
        return MemberDTO.of(member);
    }
}
