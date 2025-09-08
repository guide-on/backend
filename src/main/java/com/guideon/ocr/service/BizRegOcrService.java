package com.guideon.ocr.service;

import com.guideon.ocr.dto.BizRegOcrResultDTO;
import org.springframework.web.multipart.MultipartFile;

public interface BizRegOcrService {
    BizRegOcrResultDTO extract(MultipartFile file);
}
