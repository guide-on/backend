package com.guideon.community.exception;

import com.guideon.common.exception.NotFoundException;
public class PostNotFoundException extends NotFoundException {
    public PostNotFoundException(Long postId) {
        super("게시글을 찾을 수 없습니다. id=" + postId);
    }
}
