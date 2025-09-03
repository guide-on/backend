package com.guideon.verification.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class StatusResponse {
    private final boolean success;
    private final String message;
}
