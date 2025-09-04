package com.guideon.community.dto;

import com.guideon.community.enums.FreeType;
import lombok.Data;

import java.util.List;

@Data
public class PostUpdateRequest {
    private Long postId;
    private String title;
    private String content;
    private String thumbnailUrl;
    private FreeType freeType;         // FREE인 글만 의미
    private List<String> imageUrls;    // 전체 재설정
    private List<Long> hashtagIds;     // 전체 재설정
}
