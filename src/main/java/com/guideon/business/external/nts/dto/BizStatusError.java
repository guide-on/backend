package com.guideon.business.external.nts.dto;

public enum BizStatusError {
    INVALID_BNO,
    NOT_REGISTERED,
    INACTIVE,        // 휴업/폐업 등 운영 불가
    UNKNOWN_STATUS   // 정의 밖 코드
}
