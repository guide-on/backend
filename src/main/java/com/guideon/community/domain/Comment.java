package com.guideon.community.domain;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class Comment {
    private Long id;
    private Long postId;
    private Long memberId;
    private Long parentCommentId;
    private String content;
    private Integer depth;          // 0: 댓글, 1: 대댓글
    private Boolean isDeleted;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
