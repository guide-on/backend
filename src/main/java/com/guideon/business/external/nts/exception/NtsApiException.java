package com.guideon.business.external.nts.exception;

import lombok.Getter;

@Getter
public class NtsApiException extends RuntimeException {
    private final String code;

    public NtsApiException(String code, String message) {
        super(message);
        this.code = code;
    }

    public NtsApiException(String code, String message, Throwable cause) {
        super(message, cause);
        this.code = code;
    }

    public static NtsApiException apiError(String code, String message) {
        return new NtsApiException(code, message);
    }

    public static NtsApiException httpError(String code, Throwable cause) {
        return new NtsApiException(code, "HTTP 호출 중 오류", cause);
    }
}
