package com.guideon.document.mapper;

import com.guideon.document.domain.LoanSessionVO;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface LoanSessionMapper {

    /**
     * 대출 세션 생성
     */
    int insert(LoanSessionVO loanSession);

    /**
     * 세션 ID로 조회
     */
    LoanSessionVO selectById(Long sessionId);
}
