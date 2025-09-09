package com.guideon.member.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PreferenceDTO {
    // 시군구 코드(법정동 앞 2자리), 예: 11000
    private List<String> regionCodes;      // nullable/empty 허용 (건너뛰기)

    // KSIC 코드(2자리 숫자)
    private List<String> industryCodes;    // nullable/empty 허용

    // 업종: 라벨 ID로 선택(선택) — industryCodes가 비어있으면 라벨을 코드로 변환
    private List<String> industryTags;

    /** 공백/중복 제거 + 포맷 필터링 */
    public List<String> dedupRegions() {
        if (regionCodes == null) return Collections.emptyList();
        return regionCodes.stream()
                .filter(Objects::nonNull)
                .map(s -> s.replaceAll("\\s+", ""))
                .filter(s -> s.matches("^\\d{5}$"))
                .distinct()
                .limit(20) // 과도 입력 방지(선택)
                .collect(Collectors.toList());
    }

    public List<String> dedupIndustries() {
        return dedup(industryCodes, "^[0-9]{2}$", 20); // 2자리만 허용
    }

    private static List<String> dedup(List<String> src, String regex, int limit) {
        if (src == null) return List.of();
        return src.stream().filter(Objects::nonNull)
                .map(s -> s.replaceAll("\\s+",""))
                .filter(s -> s.matches(regex))
                .distinct().limit(limit).collect(Collectors.toList());
    }

    public List<String> safeTags() {
        if (industryTags == null) return List.of();
        return industryTags.stream().filter(Objects::nonNull)
                .map(String::trim).filter(s -> !s.isEmpty())
                .distinct().limit(50).collect(Collectors.toList());
    }
}
