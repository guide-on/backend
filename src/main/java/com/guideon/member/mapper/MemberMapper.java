package com.guideon.member.mapper;

import com.guideon.security.account.domain.MemberVO;
import org.apache.ibatis.annotations.Param;

public interface MemberMapper {
    MemberVO get(@Param("id") Long id, @Param("email") String email);
}
