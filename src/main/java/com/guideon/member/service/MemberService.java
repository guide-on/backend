package com.guideon.member.service;

import com.guideon.member.dto.MemberDTO;
import com.guideon.member.dto.MemberJoinDTO;
import com.guideon.security.account.domain.MemberVO;

public interface MemberService {
    /**
     * id 또는 이메일로 회원 정보 조회
     * @param id
     * @param email
     * @return 회원 dto
     */
    MemberDTO get(Long id, String email);    // 두 파라미터 중 하나는 null이어도 됨

    /**
     * 회원가입
     * @param member
     * @return 회원 dto
     */
    MemberDTO join(MemberJoinDTO member);

    /**
     * 이메일 존재 여부 (이메일 중복 체크)
     * @param email
     * @return 존재 여부
     */
    boolean existsByEmail(String email);

    /**
     * 회원 전화번호 조회
     * @param email
     * @return 회원 전화번호
     */
    String getPhoneByEmail(String email);

    /**
     * 회원 이메일 조회
     * @param id
     * @return 회원 이메일
     */
    String getEmailById(Long id);
}
