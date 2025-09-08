package com.guideon.community.domain;

import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.PostCategory;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class Post {
    private Long id;
    private Long memberId;
    private PostCategory category;
    private FreeType freeType;
    private String title;
    private String content;
    private String thumbnailUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer commentCount;
    private Boolean isDeleted;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
