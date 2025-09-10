package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.domain.LoanSessionVO;
import com.guideon.document.domain.PolicyVO;
import com.guideon.document.dto.LoanSessionDTO;
import com.guideon.document.dto.SessionRequest;
import com.guideon.document.mapper.BusinessInfoMapper;
import com.guideon.document.mapper.LoanSessionMapper;
import com.guideon.document.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
@RequiredArgsConstructor
public class LoanSessionServiceImpl implements LoanSessionService {

    private final BusinessInfoMapper businessInfoMapper;
    private final PolicyMapper policyMapper;
    private final LoanSessionMapper loanSessionMapper;

    @Override
    public Map<String, Object> createLoanSession(SessionRequest request) {

        log.info("대출 세션 생성 시작: businessId={}, policyId={}",
                request.getBusinessId(), request.getPolicyId());

        // 1. 사업체 정보 유효성 검증
        BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(request.getBusinessId());
        if (businessInfo == null) {
            throw new IllegalArgumentException("존재하지 않는 사업체 정보입니다. businessId: " + request.getBusinessId());
        }

        // 2. 정책자금 유효성 검증
        PolicyVO policy = policyMapper.selectByPolicyId(request.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("존재하지 않는 정책자금입니다. policyId: " + request.getPolicyId());
        }

        // 3. 기존 세션 확인
        LoanSessionVO existingSession = loanSessionMapper.selectByBusinessIdAndPolicyId(
                request.getBusinessId(), request.getPolicyId());

        if (existingSession != null) {
            // 기존 세션 재개
            log.info("기존 세션 재개: sessionId={}", existingSession.getId());

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("sessionId", existingSession.getId());
            response.put("businessId", request.getBusinessId());
            response.put("policyId", request.getPolicyId());
            response.put("sessionStatus", existingSession.getSessionStatus());
            response.put("requiredDocuments", existingSession.getRequiredDocuments());
            response.put("submittedDocuments", existingSession.getSubmittedDocuments());
            response.put("progressPercentage", existingSession.getProgressPercentage());
            response.put("isExistingSession", true);

            return response;
        }

        // 4. 새 세션 생성
        LoanSessionDTO sessionDTO = LoanSessionDTO.createDefault(
                request.getBusinessId(),
                request.getPolicyId()
        );

        LoanSessionVO loanSession = sessionDTO.toVO();
        loanSessionMapper.insert(loanSession);
        Long sessionId = loanSession.getId();

        // 5. 응답구성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", sessionId);
        response.put("businessId", request.getBusinessId());
        response.put("policyId", request.getPolicyId());
        response.put("sessionStatus", "IN_PROGRESS");
        response.put("requiredDocuments", 0);
        response.put("submittedDocuments", 0);
        response.put("progressPercentage", 0.0);

        log.info("대출 세션 생성 완료: sessionId={}", sessionId);

        return response;
    }

    @Override
    public List<LoanSessionDTO> getAllSimulationsByBusinessId(Long businessId) {
        log.info("비즈니스의 모든 시뮬레이션 조회: businessId={}", businessId);

        List<LoanSessionVO> sessions = loanSessionMapper.selectAllByBusinessId(businessId);
        return sessions.stream()
                .map(LoanSessionDTO::fromVO)
                .collect(Collectors.toList());
    }

    @Override
    public LoanSessionDTO getSessionInfo(Long sessionId) {
        log.info("세션 상세 정보 조회: sessionId={}", sessionId);

        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다: " + sessionId);
        }

        return LoanSessionDTO.fromVO(session);
    }
}
