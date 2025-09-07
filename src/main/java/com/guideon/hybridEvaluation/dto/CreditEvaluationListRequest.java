package com.guideon.hybridEvaluation.dto;

import lombok.Data;

import java.sql.Timestamp;

@Data
public class CreditEvaluationListRequest {
    private String userId;              // 특정 사용자의 평가 이력 조회
    private Timestamp startDate;        // 평가일자 범위 검색 시작
    private Timestamp endDate;          // 평가일자 범위 검색 종료
    private Integer page = 1;           // 페이지 번호 (기본값: 1)
    private Integer limit = 20;         // 페이지당 개수 (기본값: 20)
    private String sortBy = "evaluationDate";  // 정렬 기준 (기본값: evaluationDate)
    private String sortOrder = "DESC";  // 정렬 순서 (기본값: DESC)
    
    // 페이징을 위한 offset 계산
    public int getOffset() {
        return (page - 1) * limit;
    }
}
