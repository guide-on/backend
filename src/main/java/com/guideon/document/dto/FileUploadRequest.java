package com.guideon.document.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FileUploadRequest {
    private Long documentId;        // 업로드할 서류 ID
    private MultipartFile file;     // 업로드할 파일
}
