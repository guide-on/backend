package com.guideon.hybridEvaluation.exception;

import com.guideon.common.exception.BadRequestException;

public class CreditEvaluationValidationException extends BadRequestException {
    
    public CreditEvaluationValidationException(String message) {
        super(message);
    }
    
    public CreditEvaluationValidationException(String field, String value, String rule) {
        super(String.format("필드 '%s'의 값 '%s'이(가) 유효하지 않습니다. 규칙: %s", field, value, rule));
    }
}
