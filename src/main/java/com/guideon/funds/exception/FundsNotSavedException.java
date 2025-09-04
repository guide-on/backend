package com.guideon.funds.exception;

/**
 * 북마크되지 않은 정책지원금을 북마크 해제하려고 할 때 발생하는 예외
 */
public class FundsNotSavedException extends RuntimeException {
    
    public FundsNotSavedException(String message) {
        super(message);
    }
    
    public FundsNotSavedException(Long fundsId, Long memberId) {
        super("북마크되지 않은 정책지원금입니다. fundsId: " + fundsId + ", memberId: " + memberId);
    }
}
