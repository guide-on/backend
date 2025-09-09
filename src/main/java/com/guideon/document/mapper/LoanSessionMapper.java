package com.guideon.document.mapper;

import com.guideon.document.domain.LoanSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

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

    /**
     * 세션 진행 상태 업데이트
     */
    void updateSessionProgress(@Param("sessionId") Long sessionId,
                               @Param("requiredDocuments") Integer requiredDocuments,
                               @Param("submittedDocuments") Integer submittedDocuments,
                               @Param("progressPercentage") Double progressPercentage);
}
