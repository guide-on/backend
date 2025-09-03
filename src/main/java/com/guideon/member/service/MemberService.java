package com.guideon.member.service;

import com.guideon.member.dto.MemberDTO;

public interface MemberService {
    /**
     * id 또는 이메일로 회원 정보 조회
     * @param id
     * @param email
     * @return 회원 dto
     */
    MemberDTO get(Long id, String email);    // 두 파라미터 중 하나는 null이어도 됨
}
