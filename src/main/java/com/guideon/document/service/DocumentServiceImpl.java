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

import java.util.*;

@Service
@RequiredArgsConstructor
@Log4j2
public class DocumentServiceImpl implements DocumentService {

    private final BusinessInfoMapper businessInfoMapper;
    private final PolicyMapper policyMapper;
    private final LoanSessionMapper loanSessionMapper;
    private final DocumentParsingService documentParsingService;

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

        // 3. 실제 세션 생성
        LoanSessionDTO sessionDTO = LoanSessionDTO.createDefault(
                request.getBusinessId(),
                request.getPolicyId()
        );

        LoanSessionVO loanSession = sessionDTO.toVO();
        loanSessionMapper.insert(loanSession);
        Long sessionId = loanSession.getId();

        // 4. 응답 구성
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
    public Map<String, Object> getRequiredDocuments(Long sessionId) {

        log.info("서류 목록 조회 시작: sessionId={}", sessionId);

        // 1. 세션 정보 조회
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 정책자금 정보 조회
        PolicyVO policy = policyMapper.selectByPolicyId(session.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("정책자금 정보를 찾을 수 없습니다.");
        }

        // 3. 사업체 정보 조회
        BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(session.getBusinessId());
        if (businessInfo == null) {
            throw new IllegalArgumentException("사업체 정보를 찾을 수 없습니다.");
        }

        // 4. DocumentParsingService로 JSON 파싱
        List<Map<String, Object>> documentGroups = documentParsingService.parseRequiredDocuments(
            policy.getRequiredDocuments(), 
            businessInfo
        );

        // 5. 응답 구성 (순서 보장)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", sessionId);
        response.put("policyName", policy.getPolicyName());
        response.put("documentGroups", documentGroups);
        response.put("totalGroups", documentGroups.size());

        return response;
    }
}