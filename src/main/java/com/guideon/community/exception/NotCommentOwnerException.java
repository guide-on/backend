package com.guideon.community.exception;

import com.guideon.common.exception.ForbiddenException;
public class NotCommentOwnerException extends ForbiddenException {
    public NotCommentOwnerException() { super("본인 댓글만 수정/삭제할 수 있습니다."); }
}
