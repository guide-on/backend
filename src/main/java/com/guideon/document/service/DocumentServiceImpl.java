package com.guideon.document.service;

import com.guideon.document.domain.BusinessInfoVO;
import com.guideon.document.domain.DocumentUploadsVO;
import com.guideon.document.domain.LoanSessionVO;
import com.guideon.document.domain.PolicyVO;
import com.guideon.document.dto.DocumentSaveRequest;
import com.guideon.document.dto.LoanSessionDTO;
import com.guideon.document.dto.MyDataSyncRequest;
import com.guideon.document.dto.SessionRequest;
import com.guideon.document.mapper.BusinessInfoMapper;
import com.guideon.document.mapper.DocumentUploadsMapper;
import com.guideon.document.mapper.LoanSessionMapper;
import com.guideon.document.mapper.PolicyMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class DocumentServiceImpl implements DocumentService {

    private final BusinessInfoMapper businessInfoMapper;
    private final PolicyMapper policyMapper;
    private final LoanSessionMapper loanSessionMapper;
    private final DocumentParsingService documentParsingService;
    private final DocumentUploadsMapper documentUploadsMapper;

    @Override
    @Transactional
    public Map<String, Object> getRequiredDocuments(Long sessionId) {

        log.info("서류 생성 요청: sessionId={}", sessionId);

        // 1. 세션 정보 조회
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 이미 서류가 생성되었는지 체크
        List<DocumentUploadsVO> existingDocs = documentUploadsMapper.selectBySessionId(sessionId);
        if (!existingDocs.isEmpty()) {
            log.info("이미 생성된 서류 존재: sessionId={}, 서류 수={}", sessionId, existingDocs.size());

            // 정책자금 정보 조회 (정책명 포함을 위해)
            PolicyVO policy = policyMapper.selectByPolicyId(session.getPolicyId());
            String policyName = policy != null ? policy.getPolicyName() : null;

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("policyName", policyName);
            response.put("message", "서류가 이미 생성되었습니다.");
            response.put("documentsCount", existingDocs.size());
            response.put("isCreated", true);
            return response;
        }

        // 3. 정책자금 정보 조회
        PolicyVO policy = policyMapper.selectByPolicyId(session.getPolicyId());
        if (policy == null) {
            throw new IllegalArgumentException("정책자금 정보를 찾을 수 없습니다.");
        }

        // 4. 사업체 정보 조회
        BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(session.getBusinessId());
        if (businessInfo == null) {
            throw new IllegalArgumentException("사업체 정보를 찾을 수 없습니다.");
        }

        // 5. DocumentParsingService로 JSON 파싱
        List<Map<String, Object>> documentGroups;
        try {
            documentGroups = documentParsingService.parseRequiredDocuments(
                    policy.getRequiredDocuments(),
                    businessInfo
            );
        } catch (Exception e) {
            log.error("서류 파싱 실패: sessionId={}", sessionId, e);
            throw new RuntimeException("서류 정보 파싱 중 오류가 발생했습니다.");
        }

        // 6. 서류 저장 (더블 체크)
        List<DocumentUploadsVO> recheck = documentUploadsMapper.selectBySessionId(sessionId);
        if (recheck.isEmpty()) {
            try {
                saveDocuments(sessionId, documentGroups);
                log.info("서류 생성 완료: sessionId={}", sessionId);
            } catch (Exception e) {
                log.error("서류 생성 실패: sessionId={}, 오류={}", sessionId, e.getMessage());
                if (e.getMessage() != null && e.getMessage().contains("Duplicate")) {
                    log.warn("중복 생성 시도 감지: sessionId={}", sessionId);
                } else {
                    throw e;
                }
            }
        }

        // 7. 간소화된 응답 (저장 완료 확인만)
        int totalDocuments = documentGroups.stream()
                .mapToInt(group -> {
                    List<Map<String, Object>> docs = (List<Map<String, Object>>) group.get("documents");
                    return docs != null ? docs.size() : 0;
                }).sum();

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("sessionId", sessionId);
        response.put("policyName", policy.getPolicyName());
        response.put("message", "서류 생성이 완료되었습니다.");
        response.put("documentsCount", totalDocuments);
        response.put("isCreated", true);

        return response;
    }

    @Override
    public Map<String, Object> syncWithMyData(Long sessionId, MyDataSyncRequest request) {

        log.info("마이데이터 연동 시작: sessionId={}", sessionId);

        // 1. 약관 동의 검증
        if (request.getAgreements() == null || !request.getAgreements().isAllAgreed()) {
            throw new IllegalArgumentException("마이데이터 이용을 위해서는 모든 약관에 동의해야 합니다.");
        }

        // 2. 세션 검증
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다.");
        }

        // 3. 마이데이터 연동 가능한 서류들 조회
        List<Long> mydataEligibleIds = documentUploadsMapper.selectMydataEligibleIds(sessionId);

        if (mydataEligibleIds.isEmpty()) {
            throw new IllegalArgumentException("마이데이터 연동 가능한 서류가 없습니다.");
        }

        // 4. 마이데이터 연동 처리
        documentUploadsMapper.updateMyDataStatus(mydataEligibleIds);

        // 5. 응답
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", sessionId);
        response.put("syncedDocuments", mydataEligibleIds.size());
        response.put("message", "마이데이터 연동이 완료되었습니다.");

        log.info("마이데이터 연동 완료: sessionId={}, 연동된 서류 수={}", sessionId, mydataEligibleIds.size());

        return response;
    }

    @Override
    public Map<String, Object> getDocumentStatus(Long sessionId) {

        log.info("서류 상태 조회: sessionId={}", sessionId);

        // 1. 세션 검증
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 서류 목록 조회
        List<DocumentUploadsVO> documents = documentUploadsMapper.selectBySessionId(sessionId);

        // 3. 서류가 없으면 안내 응답
        if (documents.isEmpty()) {
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("sessionId", sessionId);
            response.put("policyName", "");
            response.put("documentGroups", new ArrayList<>());
            response.put("totalGroups", 0);
            response.put("totalRequirements", 0);
            response.put("completedRequirements", 0);
            response.put("progressPercentage", 0.0);
            response.put("message", "서류가 아직 생성되지 않았습니다.");
            return response;
        }

        // 4. 정책자금 및 사업체 정보 조회
        PolicyVO policy = policyMapper.selectByPolicyId(session.getPolicyId());
        BusinessInfoVO businessInfo = businessInfoMapper.selectByBusinessId(session.getBusinessId());

        if (policy == null || businessInfo == null) {
            throw new IllegalArgumentException("정책자금 또는 사업체 정보를 찾을 수 없습니다.");
        }

        // 5. JSON 파싱으로 documentGroups 구조 생성 (레이블, 설명 포함)
        List<Map<String, Object>> documentGroups;
        try {
            documentGroups = documentParsingService.parseRequiredDocuments(
                    policy.getRequiredDocuments(),
                    businessInfo
            );
        } catch (Exception e) {
            log.error("서류 파싱 실패: sessionId={}", sessionId, e);
            throw new RuntimeException("서류 정보 파싱 중 오류가 발생했습니다.");
        }

        // 6. DB 서류 데이터를 Map으로 구성 (빠른 조회용)
        Map<String, DocumentUploadsVO> documentsMap = documents.stream()
                .collect(Collectors.toMap(
                        doc -> doc.getDocumentGroup() + "_" + doc.getDocumentName(),
                        doc -> doc
                ));

        // 7. documentGroups에 실제 업로드 상태만 반영 (진행률 계산 제거)
        for (Map<String, Object> group : documentGroups) {
            String groupKey = (String) group.get("groupKey");
            Integer minSelect = (Integer) group.get("minSelect");
            List<Map<String, Object>> docs = (List<Map<String, Object>>) group.get("documents");

            if (docs != null && minSelect != null) {
                int groupCompletedCount = 0;

                // 각 서류에 실제 상태 정보 추가
                for (Map<String, Object> doc : docs) {
                    String docName = (String) doc.get("name");
                    String mapKey = groupKey + "_" + docName;

                    DocumentUploadsVO uploadedDoc = documentsMap.get(mapKey);
                    if (uploadedDoc != null) {
                        // DB의 실제 상태로 업데이트
                        doc.put("id", uploadedDoc.getId());
                        doc.put("uploadStatus", uploadedDoc.getUploadStatus());
                        doc.put("isSelected", uploadedDoc.getIsSelected());
                        doc.put("isMydataRetrieved", uploadedDoc.getIsMydataRetrieved());

                        // 파일 정보가 있으면 추가
                        if (uploadedDoc.getOriginalFilename() != null) {
                            doc.put("originalFilename", uploadedDoc.getOriginalFilename());
                            doc.put("fileSize", uploadedDoc.getFileSize());
                            doc.put("uploadedAt", uploadedDoc.getUploadedAt());
                        }

                        // 완료된 서류 카운트 (진행률 계산은 안하고 UI 표시용으로만)
                        String status = uploadedDoc.getUploadStatus();
                        if ("UPLOADED".equals(status) || "VALIDATED".equals(status)) {
                            groupCompletedCount++;
                        }

                        doc.put("status", uploadedDoc.getUploadStatus().toLowerCase());
                    } else {
                        // DB에 없으면 기본 상태
                        doc.put("uploadStatus", "PENDING");
                        doc.put("isSelected", false);
                        doc.put("isMydataRetrieved", false);
                        doc.put("status", "pending");
                    }
                }

                // 그룹에 완료 정보 추가 (UI에서 사용할 수 있도록)
                group.put("submitted", Math.min(groupCompletedCount, minSelect));
                group.put("isCompleted", groupCompletedCount >= minSelect);
            }
        }

        // 8. 응답 구성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("success", true);
        response.put("sessionId", sessionId);
        response.put("policyName", policy.getPolicyName());
        response.put("documentGroups", documentGroups);
        response.put("totalGroups", documentGroups.size());
        response.put("totalRequirements", session.getRequiredDocuments() != null ? session.getRequiredDocuments() : 0);
        response.put("completedRequirements", session.getSubmittedDocuments() != null ? session.getSubmittedDocuments() : 0);
        response.put("progressPercentage", session.getProgressPercentage() != null ? session.getProgressPercentage() : BigDecimal.ZERO);

        log.info("서류 상태 조회 완료: sessionId={}, 진행률={}%", sessionId, session.getProgressPercentage());

        return response;
    }

    @Override
    public Map<String, Object> uploadFile(Long sessionId, Long documentId, MultipartFile file) {

        log.info("파일 업로드 시작: sessionId={}, documentId={}, filename={}",
                sessionId, documentId, file.getOriginalFilename());

        // 1. 세션 검증
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 서류 정보 검증
        DocumentUploadsVO document = documentUploadsMapper.selectById(documentId);
        if (document == null) {
            throw new IllegalArgumentException("존재하지 않는 서류입니다. documentId: " + documentId);
        }

        if (!document.getSessionId().equals(sessionId)) {
            throw new IllegalArgumentException("해당 세션에 속하지 않는 서류입니다.");
        }

        // 3. 파일 검증
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("업로드할 파일이 없습니다.");
        }

        // 4. 파일 저장 처리
        try {
            String originalFilename = file.getOriginalFilename();
            String storedFilename = generateStoredFilename(originalFilename);
            String filePath = saveFile(file, storedFilename);

            // 5. 데이터베이스 업데이트
            documentUploadsMapper.updateFileInfo(
                    documentId,
                    originalFilename,
                    storedFilename,
                    filePath,
                    file.getSize(),
                    file.getContentType(),
                    "UPLOADED"
            );

            // 6. 응답 구성
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("documentId", documentId);
            response.put("documentName", document.getDocumentName());
            response.put("originalFilename", originalFilename);
            response.put("fileSize", file.getSize());
            response.put("uploadStatus", "UPLOADED");
            response.put("message", "파일 업로드가 완료되었습니다.");

            log.info("파일 업로드 완료: documentId={}, filename={}", documentId, originalFilename);

            return response;

        } catch (Exception e) {
            log.error("파일 업로드 실패: documentId={}, filename={}", documentId, file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드 중 오류가 발생했습니다: " + e.getMessage());
        }
    }

    /**
     * 파싱된 서류 정보를 DB에 저장
     */
    private void saveDocuments(Long sessionId, List<Map<String, Object>> documentGroups) {

        log.info("서류 저장 시작: sessionId={}, 그룹 수={}", sessionId, documentGroups.size());

        List<DocumentUploadsVO> documentUploads = new ArrayList<>();

        for (Map<String, Object> group : documentGroups) {
            String groupKey = (String) group.get("groupKey");
            Integer minSelect = (Integer) group.get("minSelect");
            List<Map<String, Object>> docs = (List<Map<String, Object>>) group.get("documents");

            if (docs != null) {
                for (Map<String, Object> doc : docs) {
                    DocumentSaveRequest request = DocumentSaveRequest.builder()
                            .documentGroup(groupKey)
                            .documentName((String) doc.get("name"))
                            .isMydataAvailable((Boolean) doc.get("mydataEligible"))
                            .groupMinSelect(minSelect)
                            .isSelected(minSelect != 1)  // 택1이면 false, 나머지 true
                            .build();

                    documentUploads.add(request.toVO(sessionId));
                }
            }
        }

        if (!documentUploads.isEmpty()) {
            documentUploadsMapper.insertDocuments(documentUploads);
            log.info("서류 정보 저장 성공: sessionId={}, 저장된 서류 수={}", sessionId, documentUploads.size());
        } else {
            log.warn("저장할 서류가 없음: sessionId={}", sessionId);
        }
    }

    /**
     * 저장용 파일명 생성 (중복 방지)
     */
    private String generateStoredFilename(String originalFilename) {
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        return System.currentTimeMillis() + "_" + UUID.randomUUID().toString().substring(0, 8) + extension;
    }

    /**
     * 파일 저장 (실제 파일 시스템에 저장)
     */
    private String saveFile(MultipartFile file, String storedFilename) throws IOException {
        // 환경변수에서 업로드 경로 읽기
        String uploadBase = System.getProperty("UPLOAD_BASE_PATH", "/tmp/uploads");
        String uploadDir = uploadBase + "/documents/";

        File dir = new File(uploadDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String filePath = uploadDir + storedFilename;
        File targetFile = new File(filePath);
        file.transferTo(targetFile);

        return filePath;
    }
}