package com.guideon.funds.exception;

/**
 * 정책지원금을 찾을 수 없을 때 발생하는 예외
 */
public class FundsNotFoundException extends RuntimeException {
    
    public FundsNotFoundException(String message) {
        super(message);
    }
    
    public FundsNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public FundsNotFoundException(Long fundsId) {
        super("정책지원금을 찾을 수 없습니다. ID: " + fundsId);
    }
}
