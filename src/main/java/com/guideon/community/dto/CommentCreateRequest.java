package com.guideon.community.dto;

import lombok.Data;

@Data
public class CommentCreateRequest {
    private Long postId;
    private Long parentCommentId;  // 선택(대댓글)
    private String content;
}
