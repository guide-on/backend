package com.guideon.hybridEvaluation.exception;

import com.guideon.common.exception.NotFoundException;

public class CreditEvaluationNotFoundException extends NotFoundException {
    
    public CreditEvaluationNotFoundException(String message) {
        super(message);
    }
    
    public CreditEvaluationNotFoundException(String userId, String evaluationDate) {
        super(String.format("사용자 ID '%s'의 평가일자 '%s' 신용평가 데이터를 찾을 수 없습니다.", userId, evaluationDate));
    }
    
    public static CreditEvaluationNotFoundException forUser(String userId) {
        return new CreditEvaluationNotFoundException(String.format("사용자 ID '%s'의 신용평가 데이터를 찾을 수 없습니다.", userId));
    }
}
