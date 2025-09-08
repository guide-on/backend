package com.guideon.common.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CommonResponseDTO<T> {
    private int status;
    private String message;
    private T data;

    public static <T> CommonResponseDTO<T> success(String message, T data) {
        return new CommonResponseDTO<>(200, message, data);
    }
    public static <T> CommonResponseDTO<T> success(String message) {
        return new CommonResponseDTO<>(200, message, null);
    }
    public static <T> CommonResponseDTO<T> ok(T data) {
        return new CommonResponseDTO<>(200, "OK", data);
    }
    public static <T> CommonResponseDTO<T> ok() {
        return new CommonResponseDTO<>(200, "OK", null);
    }
    public static <T> CommonResponseDTO<T> error(String message, int status) {
        return new CommonResponseDTO<>(status, message, null);
    }
}
