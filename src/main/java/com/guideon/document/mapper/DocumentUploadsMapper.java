package com.guideon.document.mapper;

import com.guideon.document.domain.DocumentUploadsVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface DocumentUploadsMapper {

    /**
     * 서류 정보 저장
     */
    void insertDocuments(List<DocumentUploadsVO> documentUploads);

    /**
     * 세션별 서류 목록 조회
     */
    List<DocumentUploadsVO> selectBySessionId(Long sessionId);

    /**
     * 세센별 특정 문서그룹 조회
     */
    DocumentUploadsVO selectBySessionIdAndGroup(@Param("sessionId") Long sessionId, @Param("documentGroup") String documentGroup);

    /**
     * 세션별 마이데이터 연동 가능한 서류 ID 조회
     */
    List<Long> selectMydataEligibleIds(Long sessionId);

    /**
     * 마이데이터 연동 상태 업데이트
     */
    void updateMyDataStatus(List<Long> documentIds);

    /**
     * 서류 ID로 조회
     */
    DocumentUploadsVO selectById(Long documentId);

    /**
     * 서류 파일 업로드 정보 업데이트
     */
    void updateFileInfo(@Param("documentId") Long documentId,
                        @Param("originalFilename") String originalFilename,
                        @Param("storedFilename") String storedFilename,
                        @Param("filePath") String filePath,
                        @Param("fileSize") Long fileSize,
                        @Param("mimeType") String mimeType,
                        @Param("uploadStatus") String uploadStatus);
}
