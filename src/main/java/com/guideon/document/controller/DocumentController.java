package com.guideon.document.controller;

import com.guideon.document.dto.MyDataSyncRequest;
import com.guideon.document.dto.SessionRequest;
import com.guideon.document.service.DocumentService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/document")
@Log4j2
public class DocumentController {

    private final DocumentService documentService;

    /**
     * 세션별 필요 서류 목록 조회
     */
    @GetMapping("/required/{sessionId}")
    public ResponseEntity<Map<String, Object>> getRequiredDocuments(
            @PathVariable Long sessionId) {

        try {
            log.info("서류 목록 조회 요청: sessionId={}", sessionId);

            Map<String, Object> result = documentService.getRequiredDocuments(sessionId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.putAll(result);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("서류 목록 조회 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 마이데이터 연동
     */
    @PostMapping("/mydata-sync/{sessionId}")
    public ResponseEntity<Map<String, Object>> syncWithMyData(
            @PathVariable Long sessionId,
            @RequestBody MyDataSyncRequest request) {

        try{
            // 3초 딜레이로 연동 느낌 연출
            Thread.sleep(3000);

            Map<String, Object> result = documentService.syncWithMyData(sessionId, request);

            return ResponseEntity.ok(result);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "연동 중 오류가 발생했습니다"
            ));
        }

    }

    /**
     * 서류 상태 조회
     */
    @GetMapping("/status/{sessionId}")
    public ResponseEntity<Map<String, Object>> getDocumentStatus(@PathVariable Long sessionId) {

        try {
            log.info("서류 상태 조회 요청: sessionId={}", sessionId);

            Map<String, Object> result = documentService.getDocumentStatus(sessionId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.putAll(result);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("서류 상태 조회 중 오류 발생: sessionId={}", sessionId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }

    /**
     * 파일 직접 업로드
     */
    @PostMapping("/upload/{sessionId}")
    public ResponseEntity<Map<String, Object>> uploadFile(
            @PathVariable Long sessionId,
            @RequestPart("documentId") String documentIdStr,
            @RequestPart("file") MultipartFile file) {

        try {
            Long documentId = Long.parseLong(documentIdStr);  // String을 Long으로 변환
            log.info("파일 업로드 요청: sessionId={}, documentId={}", sessionId, documentId);

            Map<String, Object> result = documentService.uploadFile(sessionId, documentId, file);
            return ResponseEntity.ok(result);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("파일 업로드 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "파일 업로드 중 오류가 발생했습니다."
            ));
        }
    }
}
