package com.guideon.document.dto;

import com.guideon.document.domain.DocumentUploadsVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocumentSaveRequest {
    private String documentGroup;      // 서류 그룹
    private String documentName;       // 서류명
    private Boolean isMydataAvailable; // 마이데이터 이용 여부
    private Integer groupMinSelect;    // 그룹 최소 서류 수
    private Boolean isSelected;        // 필수/선택 여부

    /**
     * DocumentUploadsVO로 변환
     */
    public DocumentUploadsVO toVO(Long sessionId) {
        DocumentUploadsVO vo = new DocumentUploadsVO();
        vo.setSessionId(sessionId);
        vo.setDocumentGroup(documentGroup);
        vo.setDocumentName(documentName);
        vo.setIsMydataAvailable(isMydataAvailable);
        vo.setIsMydataRetrieved(false);
        vo.setGroupMinSelect(groupMinSelect);
        vo.setIsSelected(isSelected);
        vo.setUploadStatus("REQUIRED");
        return vo;
    }
}
