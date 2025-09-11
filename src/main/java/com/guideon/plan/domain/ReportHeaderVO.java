package com.guideon.plan.domain;

import lombok.*;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportHeaderVO {
    private Long reportId;
    private Long memberId;
    private Long documentId;
    private Long sessionId;
    private Double totalScore;
    private List<String> strengths; // JSON
    private List<String> risks;     // JSON
    // document_uploads 조인
    private String fileName;   // original_filename
    private String filePath;   // 파일 경로
    private String uploadedAt; // 업로드 일시
}
