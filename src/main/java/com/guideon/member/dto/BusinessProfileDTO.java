package com.guideon.member.dto;

import com.guideon.member.domain.BusinessProfileVO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.ZoneId;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BusinessProfileDTO {
    private String bizRegNo;            // 10 digits (no dash)
    private String bizName;
    private LocalDate openDate;
    private String ksicCode;            // nullable
    private String businessSggCode;     // 5자리
    private String addrRoad;
    private String addrDetail;          // nullable

    public void normalize() {
        if (bizRegNo != null) bizRegNo = bizRegNo.replaceAll("\\D", "");
    }
    public void validateRequired() {
        if (bizRegNo == null || !bizRegNo.matches("\\d{10}"))
            throw new IllegalArgumentException("사업자등록번호는 숫자 10자리여야 합니다.");
        if (bizName == null || bizName.isBlank()
                || openDate == null
                || businessSggCode == null || businessSggCode.isBlank()
                || addrRoad == null || addrRoad.isBlank()) {
            throw new IllegalArgumentException("사업자 필수 항목이 누락되었습니다.");
        }
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        if (openDate.isAfter(today)) {
            throw new IllegalArgumentException("개업일은 오늘 이후일 수 없습니다.");
        }
    }

    public BusinessProfileVO toVO(Long memberId) {
        return BusinessProfileVO.builder()
                .memberId(memberId)
                .bizRegNo(bizRegNo)
                .bizName(bizName)
                .openDate(openDate)
                .ksicCode(ksicCode)
                .addrRoad(addrRoad)
                .addrDetail(addrDetail)
                .businessSggCode(businessSggCode)
                .build();
    }
}
