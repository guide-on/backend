package com.guideon.industry.code.mapper;

import com.guideon.industry.code.dto.Ksic5DTO;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface IndustryCodeMapper {
    List<Ksic5DTO> selectKsic5Paged(@Param("offset") int offset,
                                    @Param("amount") int amount,
                                    @Param("code") String code,   // 코드 prefix (숫자만)
                                    @Param("name") String name);  // 업종명 부분일치
    int countKsic5(@Param("code") String code,
                    @Param("name") String name);
}
