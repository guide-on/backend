package com.guideon.member.mapper;

import com.guideon.security.account.domain.AuthVO;
import com.guideon.security.account.domain.MemberVO;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {
    MemberVO get(@Param("id") Long id, @Param("email") String email);
    int existsEmail(@Param("email") String email);
    int existsPhone(@Param("phone") String phone);
    int insert(MemberVO member);                      // 회원정보 저장
    int insertAuth(AuthVO auth);                      // 권한정보 저장
    String findPhoneByEmail(@Param("email") String email);
    String findEmailById(@Param("id") Long id);
}
