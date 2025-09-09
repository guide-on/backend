package com.guideon.member.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BusinessProfileVO {
    private Long memberId;          // PK = FK(member.member_id)
    private String bizRegNo;        // CHAR(10)
    private String bizName;
    private LocalDate openDate;
    private String ksicCode;

    private String businessSggCode; // CHAR(5)
    private String addrRoad;
    private String addrDetail;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
