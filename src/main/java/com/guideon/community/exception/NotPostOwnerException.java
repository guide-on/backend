package com.guideon.community.exception;

import com.guideon.common.exception.ForbiddenException;
public class NotPostOwnerException extends ForbiddenException {
    public NotPostOwnerException() { super("본인 게시글만 수정/삭제할 수 있습니다."); }
}
