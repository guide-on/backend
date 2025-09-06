package com.guideon.common.pagination;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class Page<T> {
    private int totalCount;     // 전체 데이터 건수
    private int totalPage;      // 전체 페이지 수

    @JsonIgnore                 // JSON 직렬화에서 제외
    private PageRequest pageRequest;

    private List<T> list;       // 페이징된 데이터 목록

    // 정적 팩토리 메서드 - 페이지 객체 생성
    public static <T> Page of(PageRequest pageRequest, int totalCount, List<T> list) {
        // 전체 페이지 수 계산 (올림 처리)
        int totalPage = (int)Math.ceil((double)totalCount / pageRequest.getAmount());
        return new Page(totalCount, totalPage, pageRequest, list);
    }

    // 현재 페이지 번호 반환
    public int getPageNum() {
        return pageRequest.getPage();
    }

    // 페이지당 데이터 건수 반환
    public int getAmount() {
        return pageRequest.getAmount();
    }
}
