package com.guideon.common.pagination;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class PageRequest {
    private int page;   // 요청 페이지 번호 (1부터 시작)
    private int amount; // 한 페이지당 데이터 건수

    // 기본 생성자: 첫 페이지, 10개씩 표시
    public PageRequest() {
        page = 1;
        amount = 10;
    }

    // 정적 팩토리 메서드
    public static PageRequest of(int page, int amount) {
        int p = Math.max(page, 1);
        int s = amount < 1 ? 10 : Math.min(amount, 200);
        return new PageRequest(p, s);
    }

    // MyBatis LIMIT 절에 사용할 offset 값 계산
    public int getOffset() {
        return (page - 1) * amount;
    }
}
