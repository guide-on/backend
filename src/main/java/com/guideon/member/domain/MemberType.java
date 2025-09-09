package com.guideon.member.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum MemberType {
    GENERAL("GENERAL"),
    SOLE_PROPRIETOR("SOLE_PROPRIETOR");

    private final String code;
}
