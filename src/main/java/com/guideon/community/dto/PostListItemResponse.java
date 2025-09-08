package com.guideon.community.dto;

import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.PostCategory;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PostListItemResponse {
    private Long id;
    private PostCategory category;
    private FreeType freeType;
    private String title;
    private String contentPreview;
    private String thumbnailUrl;
    private Integer viewCount;
    private Integer likeCount;
    private Integer bookmarkCount;
    private Integer commentCount;
    private Timestamp createdAt;
    private List<HashtagDto> hashtags;
}
