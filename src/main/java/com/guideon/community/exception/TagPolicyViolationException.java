package com.guideon.community.exception;

import com.guideon.common.exception.BadRequestException;
public class TagPolicyViolationException extends BadRequestException {
    public TagPolicyViolationException(String message) { super(message); }
}
