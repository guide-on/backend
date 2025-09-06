package com.guideon.funds.mapper;

import com.guideon.funds.domain.Funds;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 정책지원금 Mapper (support_fund 테이블)
 */
@Mapper
public interface FundsMapper {
    
    /**
     * 정책지원금 전체 목록 조회
     */
    List<Funds> selectAllFunds();
    
    /**
     * 정책지원금 상세 조회
     */
    Funds selectFundsById(@Param("id") Long id);

    /**
     * 정책지원금 이름으로 검색
     */
    List<Funds> selectFundsByNameLike(@Param("keyword") String keyword);
    
    /**
     * 회원별 북마크된 정책지원금 ID 목록 조회
     */
    List<Long> selectSavedFundsIdsByMemberId(@Param("memberId") Long memberId);

}
