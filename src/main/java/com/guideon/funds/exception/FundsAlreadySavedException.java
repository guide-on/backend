package com.guideon.funds.exception;

/**
 * 이미 북마크된 정책지원금을 다시 북마크하려고 할 때 발생하는 예외
 */
public class FundsAlreadySavedException extends RuntimeException {
    
    public FundsAlreadySavedException(String message) {
        super(message);
    }
    
    public FundsAlreadySavedException(Long fundsId, Long memberId) {
        super("이미 북마크된 정책지원금입니다. fundsId: " + fundsId + ", memberId: " + memberId);
    }
}
