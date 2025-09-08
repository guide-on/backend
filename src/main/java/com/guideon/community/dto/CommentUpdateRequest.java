package com.guideon.community.dto;

import lombok.Data;

@Data
public class CommentUpdateRequest {
    private Long commentId;
    private String content;
}
