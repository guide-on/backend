package com.guideon.community.domain;

import lombok.Data;
import java.sql.Timestamp;

@Data
public class PostImage {
    private Long id;
    private Long postId;
    private String imageUrl;
    private Integer sortOrder;
    private Timestamp createdAt;
}
