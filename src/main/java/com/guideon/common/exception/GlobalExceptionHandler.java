package com.guideon.common.exception;

import com.guideon.common.dto.CommonResponseDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;

@Slf4j
@ControllerAdvice
@Order(1)
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleBase(BaseException e) {
        log.error("BaseException: {}", e.getMessage());
        return ResponseEntity.status(e.getStatus())
                .body(CommonResponseDTO.error(e.getMessage(), e.getStatus()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleValidation(MethodArgumentNotValidException e) {
        String msg = e.getBindingResult().getFieldErrors().stream()
                .findFirst().map(fe -> fe.getField() + " " + fe.getDefaultMessage())
                .orElse("유효성 검사 실패");
        return ResponseEntity.status(400).body(CommonResponseDTO.error("잘못된 요청: " + msg, 400));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleIllegal(IllegalArgumentException e) {
        return ResponseEntity.status(400).body(CommonResponseDTO.error("잘못된 요청: " + e.getMessage(), 400));
    }

    @ExceptionHandler(SecurityException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleSecurity(SecurityException e) {
        return ResponseEntity.status(403).body(CommonResponseDTO.error("권한이 없습니다: " + e.getMessage(), 403));
    }

    @ExceptionHandler(RuntimeException.class)
    @ResponseBody
    public ResponseEntity<CommonResponseDTO<Void>> handleRuntime(RuntimeException e) {
        log.error("Unhandled RuntimeException", e);
        return ResponseEntity.status(500).body(CommonResponseDTO.error("서버 오류: " + e.getMessage(), 500));
    }
}
