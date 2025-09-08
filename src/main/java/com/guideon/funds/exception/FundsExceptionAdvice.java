package com.guideon.funds.exception;

import com.guideon.common.dto.CommonResponseDTO;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Funds 관련 예외 처리 Advice
 */
@Log4j2
@RestControllerAdvice(basePackages = "com.guideon.funds")
public class FundsExceptionAdvice {

    @ExceptionHandler(FundsNotFoundException.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleFundsNotFoundException(FundsNotFoundException e) {
        log.error("FundsNotFoundException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(CommonResponseDTO.error("정책지원금을 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value()));
    }

    @ExceptionHandler(FundsAlreadySavedException.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleFundsAlreadySavedException(FundsAlreadySavedException e) {
        log.error("FundsAlreadySavedException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(CommonResponseDTO.error("이미 북마크된 정책지원금입니다.", HttpStatus.CONFLICT.value()));
    }

    @ExceptionHandler(FundsNotSavedException.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleFundsNotSavedException(FundsNotSavedException e) {
        log.error("FundsNotSavedException: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponseDTO.error("북마크되지 않은 정책지원금입니다.", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleIllegalArgumentException(IllegalArgumentException e) {
        log.error("IllegalArgumentException in Funds: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(CommonResponseDTO.error("잘못된 요청입니다.", HttpStatus.BAD_REQUEST.value()));
    }

    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleIllegalStateException(IllegalStateException e) {
        log.error("IllegalStateException in Funds: {}", e.getMessage());

        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(CommonResponseDTO.error(e.getMessage(), HttpStatus.UNAUTHORIZED.value()));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<CommonResponseDTO<Void>> handleGeneralException(Exception e) {
        log.error("Unexpected exception in Funds: {}", e.getMessage(), e);

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(CommonResponseDTO.error("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요.", HttpStatus.INTERNAL_SERVER_ERROR.value()));
    }
}