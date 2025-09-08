package com.guideon.community.dto;

import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.PostCategory;
import lombok.Data;

import java.util.List;

@Data
public class PostCreateRequest {
    private PostCategory category;
    private FreeType freeType;                 // category=FREE 일 때 필수
    private String title;
    private String content;
    private String thumbnailUrl;
    private List<String> imageUrls;            // 선택
    private List<Long> hashtagIds;             // 선택
}
