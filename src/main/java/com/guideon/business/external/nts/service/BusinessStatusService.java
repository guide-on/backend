package com.guideon.business.external.nts.service;

import com.guideon.business.external.nts.client.NtsBusinessClient;
import com.guideon.business.external.nts.dto.BusinessStatusItem;
import com.guideon.business.external.nts.dto.BusinessStatusRequest;
import com.guideon.business.external.nts.dto.BusinessStatusResponse;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;


/**
 * - 입력 정규화(숫자10자리), 100개 단위 분할 호출
 * - '계속사업자' 여부 도출 헬퍼 제공
 */
@Service
@RequiredArgsConstructor
public class BusinessStatusService {
    private final NtsBusinessClient client;

    /** 단일 사업자번호 운영중 여부만 반환 (계속사업자=01) */
    public Optional<ActiveStatus> isActiveBusiness(String rawBizNo) {
        String bno = normalize(rawBizNo);
        if (bno == null) return Optional.empty();

        BusinessStatusResponse resp = client.fetchStatus(new BusinessStatusRequest(List.of(bno)));
        BusinessStatusItem item = (resp.getData() != null && !resp.getData().isEmpty())
                ? resp.getData().get(0) : null;

        if (item == null) return Optional.empty();
        boolean active = "01".equals(item.getB_stt_cd());
        return Optional.of(new ActiveStatus(bno, active, item.getB_stt_cd(), item.getB_stt()));
    }

    public BusinessStatusResponse checkStatus(List<String> rawBizNos) {
        // 1) 정규화 + 중복 제거
        List<String> cleaned = Optional.ofNullable(rawBizNos).orElse(List.of())
                .stream()
                .filter(Objects::nonNull)
                .map(this::normalize) // 숫자만
                .filter(s -> s.length() == 10)     // 10자리만 허용
                .distinct()
                .collect(Collectors.toList());

        // 2) 유효 값 없으면 빈 응답 반환
        if (cleaned.isEmpty()) {
            BusinessStatusResponse empty = new BusinessStatusResponse();
            empty.setStatus_code("OK");
            empty.setMatch_cnt(0);
            empty.setRequest_cnt(0);
            empty.setData(Collections.emptyList());
            return empty;
        }

        // 100개 단위로 분할 호출 후 병합
        List<BusinessStatusItem> merged = new ArrayList<>();
        for (int i = 0; i < cleaned.size(); i += 100) {
            List<String> chunk = cleaned.subList(i, Math.min(i + 100, cleaned.size()));
            BusinessStatusResponse resp = client.fetchStatus(new BusinessStatusRequest(chunk));
            if (resp != null && resp.getData() != null) {
                merged.addAll(resp.getData());
            }
        }

        // 병합 응답 구성
        BusinessStatusResponse out = new BusinessStatusResponse();
        out.setStatus_code("OK");
        out.setRequest_cnt(cleaned.size());
        out.setMatch_cnt(merged.size());
        out.setData(merged);
        return out;
    }

    /** 숫자만 10자리로 정규화(하이픈 등 제거). 유효하지 않으면 null */
    public String normalize(String raw) {
        if (raw == null) return null;
        String onlyDigits = raw.replaceAll("\\D", "");
        return onlyDigits.length() == 10 ? onlyDigits : null;
    }

    /** 프론트로 돌려줄 때 쓰기 좋은 응답 뷰 */
    @Value
    public static class ActiveStatus {
        String bno;
        boolean active;  // true=계속사업자
        String code;     // b_stt_cd
        String label;    // b_stt
    }
}
