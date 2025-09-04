package com.guideon.funds.mapper;

import com.guideon.funds.domain.SavedFunds;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 북마크한 정책지원금 Mapper
 */
@Mapper
public interface SavedFundsMapper {
    
    /**
     * 정책지원금 북마크 추가
     */
    int insertSavedFunds(@Param("memberId") Long memberId, @Param("fundsId") Long fundsId);
    
    /**
     * 정책지원금 북마크 해제
     */
    int deleteSavedFunds(@Param("memberId") Long memberId, @Param("fundsId") Long fundsId);
    
    /**
     * 회원별 북마크한 정책지원금 목록 조회 (support_fund 정보 포함)
     */
    List<SavedFunds> selectSavedFundsByMemberId(@Param("memberId") Long memberId);
    
    /**
     * 특정 회원의 특정 정책지원금 북마크 여부 확인
     */
    boolean existsSavedFunds(@Param("memberId") Long memberId, @Param("fundsId") Long fundsId);
    
    /**
     * 회원별 북마크 개수
     */
    int countSavedFundsByMemberId(@Param("memberId") Long memberId);
}
