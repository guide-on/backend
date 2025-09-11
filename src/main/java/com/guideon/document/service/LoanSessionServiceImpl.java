package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.domain.DocumentUploadsVO;
import com.guideon.document.domain.LoanSessionVO;
import com.guideon.document.domain.PolicyVO;
import com.guideon.document.dto.DocumentResultDTO;
import com.guideon.document.dto.LoanSessionDTO;
import com.guideon.document.dto.SessionRequest;
import com.guideon.document.mapper.BusinessInfoMapper;
import com.guideon.document.mapper.DocumentUploadsMapper;
import com.guideon.document.mapper.LoanSessionMapper;
import com.guideon.document.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
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
    private final DocumentUploadsMapper documentUploadsMapper;
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

    /**
     * 세션 결과 생성
     */
    @Override
    public Map<String, Object> createDocsResult(SessionRequest request) {
        // 1. 기존 세션 조회
        LoanSessionVO session = loanSessionMapper.selectByBusinessIdAndPolicyId(
                request.getBusinessId(), request.getPolicyId());

        if (session == null) {
            throw new IllegalArgumentException("해당 세션을 찾을 수 없습니다.");
        }

        // 2. 정책자금 정보 조회
        PolicyVO policy = policyMapper.selectByPolicyId(request.getPolicyId());

        // 3. 사업체 정보 조회
        BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(request.getBusinessId());
        if (businessInfo == null) {
            throw new IllegalArgumentException("사업체 정보를 찾을 수 없습니다.");
        }

        // 4. 세션 결과 생성
        DocumentResultDTO documentResult = DocumentResultDTO.builder()
                .policyName(policy != null ? policy.getPolicyName() : "정책자금")
                .sessionStatus(session.getSessionStatus())
                .progressPercentage(session.getProgressPercentage())
                .step("DOCS")
                .build();

        // 5. simulation_result 테이블에 저장 (session_id를 PK로 사용)
        boolean success = false;
        try {
            int result = loanSessionMapper.insertDocumentResultToSimulation(
                    session.getId(), // sessionId를 PK로 사용
                    businessInfo.getMemberId(),
                    documentResult
            );
            success = (result > 0);
            log.info("DocumentResult 저장 완료: sessionId={}, success={}", session.getId(), success);
        } catch (Exception e) {
            log.error("DocumentResult 저장 실패: sessionId={}", session.getId(), e);
            success = false;
        }

        // 6. 응답 구성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", session.getId());
        response.put("documentResult", documentResult);
        response.put("success", success);

        return response;
    }

    /**
     * 서류 등록 결과 업데이트
     */
    @Override
    public void updateDocumentResult(Long sessionId, DocumentResultDTO documentResult) {
        // 1. 세션 존재 확인
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 진행률 계산
        calculateCurrentProgress(sessionId);

        // 3. 업데이트된 세션 정보로 세션 결과 구성
        LoanSessionVO updatedSession = loanSessionMapper.selectById(sessionId);
        documentResult.setProgressPercentage(updatedSession.getProgressPercentage());
        documentResult.setSessionStatus(updatedSession.getSessionStatus());
        documentResult.setStep("DOCS");

        // 4. simulation_result 테이블 업데이트
        try {
            int result = loanSessionMapper.updateDocumentResultInSimulation(
                    sessionId, // sessionId로 직접 업데이트
                    documentResult
            );

            if (result > 0) {
                log.info("DocumentResult 업데이트 완료: sessionId={}, progress={}",
                        sessionId, documentResult.getProgressPercentage());
            } else {
                log.warn("DocumentResult 업데이트 대상 없음: sessionId={}", sessionId);
            }
        } catch (Exception e) {
            log.error("DocumentResult 업데이트 실패: sessionId={}", sessionId, e);
            throw new RuntimeException("서류 결과 업데이트에 실패했습니다.", e);
        }
    }

    /**
     * 현재 가이드의 단계 조회
     */
    @Override
    public String getCurrentStep(Long sessionId) {
        String currentStep = loanSessionMapper.selectSimulationCurrentStep(sessionId);

        if (currentStep == null) {
            throw new IllegalArgumentException("해당 세션의 시뮬레이션 정보를 찾을 수 없습니다. sessionId: " + sessionId);
        }

        log.info("현재 단계 조회: sessionId={}, currentStep={}", sessionId, currentStep);
        return currentStep;
    }
    /**
     * 세션의 현재 진행률을 계산하고 loan_sessions 테이블 업데이트
     * @param sessionId
     */
    private void calculateCurrentProgress(Long sessionId) {
        try {
            log.info("진행률 자동 계산 시작: sessionId={}", sessionId);

            // 1. 세션 정보 조회
            LoanSessionVO session = loanSessionMapper.selectById(sessionId);
            if (session == null) return;

            // 2. 정책자금 및 사업체 정보 조회
            PolicyVO policy = policyMapper.selectByPolicyId(session.getPolicyId());
            BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(session.getBusinessId());
            if (policy == null || businessInfo == null) return;

            // 3. 서류 목록 조회
            List<DocumentUploadsVO> documents = documentUploadsMapper.selectBySessionId(sessionId);
            if (documents.isEmpty()) return;

            // 4. JSON 파싱
            List<Map<String, Object>> documentGroups = documentParsingService.parseRequiredDocuments(
                    policy.getRequiredDocuments(), businessInfo);

            // 5. 서류 매핑
            Map<String, DocumentUploadsVO> documentsMap = documents.stream()
                    .collect(Collectors.toMap(
                            doc -> doc.getDocumentGroup() + "_" + doc.getDocumentName(),
                            doc -> doc
                    ));

            // 6. 올바른 진행률 계산 (getDocumentStatus와 동일한 로직)
            int totalRequirements = 0;
            int completedRequirements = 0;

            for (Map<String, Object> group : documentGroups) {
                String groupKey = (String) group.get("groupKey");
                Integer minSelect = (Integer) group.get("minSelect");
                List<Map<String, Object>> docs = (List<Map<String, Object>>) group.get("documents");

                if (docs != null && minSelect != null) {
                    totalRequirements += minSelect;  // 각 그룹의 minSelect 합

                    int groupCompletedCount = 0;

                    for (Map<String, Object> doc : docs) {
                        String docName = (String) doc.get("name");
                        String mapKey = groupKey + "_" + docName;

                        DocumentUploadsVO uploadedDoc = documentsMap.get(mapKey);
                        if (uploadedDoc != null) {
                            String status = uploadedDoc.getUploadStatus();
                            if ("UPLOADED".equals(status) || "VALIDATED".equals(status)) {
                                groupCompletedCount++;
                            }
                        }
                    }

                    // 그룹별 완료 수는 최소 선택 수로 제한
                    completedRequirements += Math.min(groupCompletedCount, minSelect);
                }
            }

            // 7. 진행률 계산
            double progressPercentage = totalRequirements > 0 ?
                    Math.round((double) completedRequirements / totalRequirements * 100.0 * 100.0) / 100.0 : 0.0;

            // 8. loan_sessions 테이블 업데이트
            loanSessionMapper.updateSessionProgress(sessionId, totalRequirements, completedRequirements, progressPercentage);

            log.info("진행률 자동 계산 완료: sessionId={}, progress={}% ({}/{})",
                    sessionId, progressPercentage, completedRequirements, totalRequirements);

        } catch (Exception e) {
            log.error("진행률 계산 실패: sessionId={}", sessionId, e);
        }
    }
}
