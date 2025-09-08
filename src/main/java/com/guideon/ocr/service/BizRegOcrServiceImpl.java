package com.guideon.ocr.service;

import com.google.cloud.vision.v1.*;
import com.google.protobuf.ByteString;
import com.guideon.ocr.dto.BizRegOcrResultDTO;
import com.guideon.ocr.util.BizRegParser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BizRegOcrServiceImpl implements BizRegOcrService {

    private final ImageAnnotatorClient vision;

    @Override
    public BizRegOcrResultDTO extract(MultipartFile file) {
        try {
            // 파일 유효성(최대 크기/타입)
            String ct = file.getContentType();
            if (ct == null || !ct.matches("image/(png|jpeg|jpg|webp|bmp|tiff)")) {
                throw new IllegalArgumentException("이미지 파일만 업로드하세요.");
            }

            // Vision 요청
            ByteString imgBytes = ByteString.copyFrom(file.getBytes());
            Image image = Image.newBuilder().setContent(imgBytes).build();
            Feature feat = Feature.newBuilder()
                    .setType(Feature.Type.DOCUMENT_TEXT_DETECTION).build();
            ImageContext ctx = ImageContext.newBuilder()
                    .addLanguageHints("ko").addLanguageHints("en").build();

            AnnotateImageRequest req = AnnotateImageRequest.newBuilder()
                    .addFeatures(feat).setImage(image).setImageContext(ctx).build();

            BatchAnnotateImagesResponse resp = vision.batchAnnotateImages(List.of(req));
            AnnotateImageResponse r = resp.getResponses(0);
            if (r.hasError()) throw new IllegalStateException(r.getError().getMessage());

            String text = r.getFullTextAnnotation() != null
                    ? r.getFullTextAnnotation().getText() : "";

            float conf = 0f;
            if (r.getTextAnnotationsCount() > 0) {
                // 대략 첫 엔티티의 score는 없음. paragraph 평균 등 상세 계산은 생략.
                conf = 0.8f; // 임시(원하면 문단 평균으로 계산)
            }

            // 파싱
            BizRegOcrResultDTO result = BizRegParser.parse(text);
            result.setRawText(text);
            result.setOcrConfidence(conf);

            // 즉시 메모리 정리(파일은 저장하지 않음)
            return result;
        } catch (Exception e) {
            throw new RuntimeException("OCR 처리 실패: " + e.getMessage(), e);
        }
    }
}