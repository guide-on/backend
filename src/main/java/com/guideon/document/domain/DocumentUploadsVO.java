package com.guideon.document.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DocumentUploadsVO {
    private Long id;
    private Long sessionId;
    private String documentGroup;
    private String documentName;    // 서류명
    private Boolean isMydataAvailable;
    private Boolean isMydataRetrieved;
    private String originalFilename;
    private String storedFilename;
    private String filePath;
    private Long fileSize;
    private String mimeType;
    private String uploadStatus;
    private String validationResult;
    private String errorMessage;

    private Integer groupMinSelect; // 그룹 별 최소 개수
    private Boolean isSelected; // 선택 여부 (필수 서류의 경우 모두 true)

    private LocalDateTime uploadedAt;
    private LocalDateTime validatedAt;


}