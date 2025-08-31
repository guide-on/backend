package com.guideon.community.mapper;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface MemberLookupMapper {
    Long findMemberIdByEmail(@Param("email") String email);
}
