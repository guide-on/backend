package com.guideon.funds.service;

import com.guideon.funds.dto.FundsDetailResponseDTO;
import com.guideon.funds.dto.FundsListResponseDTO;
import com.guideon.funds.dto.SavedFundsResponseDTO;

import java.util.List;

/**
 * 정책지원금 서비스 인터페이스
 */
public interface FundsService {

    /**
     * 정책지원금 전체 목록 조회
     */
    List<FundsListResponseDTO> getAllFunds();

    /**
     * 정책지원금 상세 조회
     */
    FundsDetailResponseDTO getFundsDetail(Long fundsId);
    /**
     * 정책지원금 이름으로 검색
     */
    List<FundsListResponseDTO> searchFundsByName(String keyword);

    /**
     * 정책지원금 북마크 추가
     */
    void saveFunds(Long fundsId);

    /**
     * 정책지원금 북마크 해제
     */
    void unsaveFunds(Long fundsId);

    /**
     * 회원별 북마크한 정책지원금 목록 조회
     */
    List<SavedFundsResponseDTO> getSavedFundsList();
}