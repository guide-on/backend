package com.guideon.funds.service;

import com.guideon.funds.domain.Funds;
import com.guideon.funds.domain.SavedFunds;
import com.guideon.funds.dto.FundsDetailResponseDTO;
import com.guideon.funds.dto.FundsListResponseDTO;
import com.guideon.funds.dto.SavedFundsResponseDTO;
import com.guideon.funds.exception.FundsAlreadySavedException;
import com.guideon.funds.exception.FundsNotFoundException;
import com.guideon.funds.exception.FundsNotSavedException;
import com.guideon.funds.mapper.FundsMapper;
import com.guideon.funds.mapper.SavedFundsMapper;
import com.guideon.security.util.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 정책지원금 서비스 구현체
 */
@Log4j2
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FundsServiceImpl implements FundsService {

    private final FundsMapper fundsMapper;
    private final SavedFundsMapper savedFundsMapper;
    private final LoginUserProvider loginUserProvider;

    @Override
    public List<FundsListResponseDTO> getAllFunds() {
        // 모든 정책지원금 조회
        List<Funds> fundsList = fundsMapper.selectAllFunds();
        return buildFundsListResponse(fundsList);
    }

    @Override
    public FundsDetailResponseDTO getFundsDetail(Long fundsId) {
        Funds funds = fundsMapper.selectFundsById(fundsId);
        if (funds == null) {
            throw new FundsNotFoundException(fundsId);
        }

        FundsDetailResponseDTO dto = FundsDetailResponseDTO.from(funds);

        // 로그인한 사용자의 북마크 여부 확인
        Long memberId = getCurrentMemberId();
        if (memberId != null) {
            boolean isSaved = savedFundsMapper.existsSavedFunds(memberId, fundsId);
            dto.setSaved(isSaved);
        }

        return dto;
    }

    @Override
    public List<FundsListResponseDTO> searchFundsByName(String keyword) {

        if (keyword == null || keyword.trim().isEmpty()) {
            throw new IllegalArgumentException("검색 키워드는 필수입니다.");
        }

        List<Funds> fundsList = fundsMapper.selectFundsByNameLike(keyword.trim());
        return buildFundsListResponse(fundsList);
    }

    /**
     * FundsList 공통 응답 빌더 (북마크 여부 포함)
     */
    private List<FundsListResponseDTO> buildFundsListResponse(List<Funds> fundsList) {
        // 로그인한 사용자 정보 획득
        Long memberId = getCurrentMemberId();

        // 해당 회원이 북마크한 정책지원금 ID 목록 조회
        Set<Long> savedFundsIds = null;
        if (memberId != null) {
            savedFundsIds = fundsMapper.selectSavedFundsIdsByMemberId(memberId)
                    .stream()
                    .collect(Collectors.toSet());
        }

        // DTO 변환 및 북마크 여부 설정
        final Set<Long> finalSavedFundsIds = savedFundsIds;
        return fundsList.stream()
                .map(funds -> {
                    FundsListResponseDTO dto = FundsListResponseDTO.from(funds);
                    if (finalSavedFundsIds != null) {
                        dto.setSaved(finalSavedFundsIds.contains(funds.getId()));
                    }
                    return dto;
                })
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void saveFunds(Long fundsId) {
        Long memberId = getCurrentMemberIdRequired();

        // 정책지원금 존재 여부 확인
        Funds funds = fundsMapper.selectFundsById(fundsId);
        if (funds == null) {
            throw new FundsNotFoundException(fundsId);
        }

        // 이미 북마크되어 있는지 확인
        if (savedFundsMapper.existsSavedFunds(memberId, fundsId)) {
            throw new FundsAlreadySavedException(fundsId, memberId);
        }

        int result = savedFundsMapper.insertSavedFunds(memberId, fundsId);
        if (result != 1) {
            throw new RuntimeException("북마크 추가에 실패했습니다.");
        }
    }

    @Override
    @Transactional
    public void unsaveFunds(Long fundsId) {
        Long memberId = getCurrentMemberIdRequired();

        // 북마크되어 있는지 확인
        if (!savedFundsMapper.existsSavedFunds(memberId, fundsId)) {
            throw new FundsNotSavedException(fundsId, memberId);
        }

        int result = savedFundsMapper.deleteSavedFunds(memberId, fundsId);
        if (result != 1) {
            throw new RuntimeException("북마크 해제에 실패했습니다.");
        }
    }

    @Override
    public List<SavedFundsResponseDTO> getSavedFundsList() {
        Long memberId = getCurrentMemberIdRequired(); // 인증 필수
        List<SavedFunds> savedFundsList = savedFundsMapper.selectSavedFundsByMemberId(memberId);

        return savedFundsList.stream()
                .map(SavedFundsResponseDTO::from)
                .collect(Collectors.toList());
    }

    /**
     * 현재 로그인한 사용자 ID 반환 (로그인하지 않은 경우 null)
     */
    private Long getCurrentMemberId() {
        try {
            return loginUserProvider.getLoginMemberId();
        } catch (Exception e) {
            log.debug("User not authenticated: {}", e.getMessage());
            return null;
        }
    }

    /**
     * 현재 로그인한 사용자 ID 반환 (로그인하지 않은 경우 예외 발생)
     */
    private Long getCurrentMemberIdRequired() {
        Long memberId = getCurrentMemberId();
        if (memberId == null) {
            throw new IllegalStateException("로그인이 필요한 서비스입니다.");
        }
        return memberId;
    }
}