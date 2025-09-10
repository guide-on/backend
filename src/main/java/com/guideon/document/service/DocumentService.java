package com.guideon.document.service;

import com.guideon.document.dto.MyDataSyncRequest;
import com.guideon.document.dto.SessionRequest;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

public interface DocumentService {

    /**
     * 세션별 필요 서류 목록 저장
     * @param sessionId 세션 아이디
     * @return 해당 세션에 대한 필요 서류 저장 성공 알림
     */
    Map<String, Object> getRequiredDocuments(Long sessionId);

    /**
     * 마이데이터 연동
     * @param sessionId 세션 아이디
     * @param request 마이데이터 연동 동의 여부
     * @return 업데이트된 서류 정보
     */
    Map<String, Object> syncWithMyData(Long sessionId, MyDataSyncRequest request);

    /**
     * 세션별 필요 서류 상태 조회
     * @param sessionId 세션 아이디
     * @return 서류 제출 상태
     */
    Map<String, Object> getDocumentStatus(Long sessionId);

    /**
     * 파일 직접 업로드
     */
    Map<String, Object> uploadFile(Long sessionId, Long documentId, MultipartFile file);
}