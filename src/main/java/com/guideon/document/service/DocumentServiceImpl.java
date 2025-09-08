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
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
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

        // 5. 서류 정보를 DB에 저장 (중복 체크)
        List<DocumentUploadsVO> existingDocs = documentUploadsMapper.selectBySessionId(sessionId);
        if (existingDocs.isEmpty()) {
            saveDocuments(sessionId, documentGroups);
            log.info("서류 정보 저장 완료: sessionId={}", sessionId);
        } else {
            log.info("이미 저장된 서류 목록 존재: sessionId={}, 기존 서류 수={}", sessionId, existingDocs.size());
        }

        // 5. 응답 구성 (순서 보장)
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", sessionId);
        response.put("policyName", policy.getPolicyName());
        response.put("documentGroups", documentGroups);
        response.put("totalGroups", documentGroups.size());

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

        log.info("서류 상태 조회 시작: sessionId={}", sessionId);

        // 1. 세션 검증
        LoanSessionVO session = loanSessionMapper.selectById(sessionId);
        if (session == null) {
            throw new IllegalArgumentException("존재하지 않는 세션입니다. sessionId: " + sessionId);
        }

        // 2. 해당 세션의 모든 서류 조회
        List<DocumentUploadsVO> documents = documentUploadsMapper.selectBySessionId(sessionId);

        // 3. 그룹별로 서류 분류
        Map<String, List<DocumentUploadsVO>> groupedDocuments = documents.stream()
                .collect(Collectors.groupingBy(DocumentUploadsVO::getDocumentGroup));

        // 4. 그룹별 상태 계산
        List<Map<String, Object>> groupStatus = new ArrayList<>();
        int totalRequirements = 0;
        int completedRequirements = 0;

        for (Map.Entry<String, List<DocumentUploadsVO>> entry : groupedDocuments.entrySet()) {
            String groupKey = entry.getKey();
            List<DocumentUploadsVO> groupDocs = entry.getValue();

            // 그룹 정보 (첫 번째 서류에서 추출)
            DocumentUploadsVO firstDoc = groupDocs.get(0);
            Integer minSelect = firstDoc.getGroupMinSelect();

            // 실제 제출된 서류 수 계산
            int actualSubmittedCount = (int) groupDocs.stream()
                    .filter(doc -> "UPLOADED".equals(doc.getUploadStatus()) || "VALIDATED".equals(doc.getUploadStatus()))
                    .count();

            // 표시용 제출 수 (택1 그룹은 minSelect로 제한)
            int displaySubmittedCount = Math.min(actualSubmittedCount, minSelect);

            // 그룹 완료 여부
            boolean isCompleted = actualSubmittedCount >= minSelect;

            // 진행률 계산을 위한 카운트
            totalRequirements += minSelect;
            if (isCompleted) {
                completedRequirements += minSelect;
            } else {
                completedRequirements += displaySubmittedCount;
            }

            // 서류별 상세 정보
            List<Map<String, Object>> documentDetails = groupDocs.stream()
                    .map(doc -> {
                        Map<String, Object> detail = new LinkedHashMap<>();
                        detail.put("id", doc.getId());
                        detail.put("documentName", doc.getDocumentName());
                        detail.put("uploadStatus", doc.getUploadStatus());
                        detail.put("isSelected", doc.getIsSelected());
                        detail.put("isMydataRetrieved", doc.getIsMydataRetrieved());
                        detail.put("isMydataAvailable", doc.getIsMydataAvailable());
                        return detail;
                    })
                    .toList();

            // 그룹 상태 정보
            Map<String, Object> groupInfo = new LinkedHashMap<>();
            groupInfo.put("groupKey", groupKey);
            groupInfo.put("minSelect", minSelect);
            groupInfo.put("submitted", displaySubmittedCount);  // 조정된 제출 수
            groupInfo.put("isCompleted", isCompleted);
            groupInfo.put("documents", documentDetails);

            groupStatus.add(groupInfo);
        }

        // 5. 전체 진행률 계산
        double progressPercentage = totalRequirements > 0 ?
                Math.round((double) completedRequirements / totalRequirements * 100.0 * 100.0) / 100.0 : 0.0;

        // 6. 응답 구성
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("sessionId", sessionId);
        response.put("totalRequirements", totalRequirements);
        response.put("completedRequirements", completedRequirements);
        response.put("progressPercentage", progressPercentage);
        response.put("groupStatus", groupStatus);

        // 7. loan_sessions 테이블 업데이트 추가
        loanSessionMapper.updateSessionProgress(sessionId, totalRequirements, completedRequirements, progressPercentage);

        log.info("서류 상태 조회 완료: sessionId={}, 진행률={}%", sessionId, progressPercentage);

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


    /**
     * 파싱된 서류 정보를 DB에 저장
     */
    private void saveDocuments(Long sessionId, List<Map<String, Object>> documentGroups) {

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
            log.info("서류 정보 저장 완료: sessionId={}, 저장된 서류 수={}", sessionId, documentUploads.size());
        }
    }
}