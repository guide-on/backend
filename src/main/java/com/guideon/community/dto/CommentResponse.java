package com.guideon.community.dto;

import lombok.Data;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Data
public class CommentResponse {
    private Long id;
    private Long postId;
    private Long memberId;
    private Long parentCommentId;
    private String content;
    private Integer depth;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<CommentResponse> children = new ArrayList<>();
}
