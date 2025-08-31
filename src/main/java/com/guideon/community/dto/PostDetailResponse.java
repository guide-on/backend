package com.guideon.community.dto;

import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.PostCategory;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class PostDetailResponse {
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
    private Timestamp createdAt;
    private Timestamp updatedAt;
    private List<String> images;
    private List<HashtagDto> hashtags;
    private List<CommentResponse> comments; // 댓글 포함
}
