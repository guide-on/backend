package com.guideon.community.dto;

import com.guideon.community.enums.FreeType;
import com.guideon.community.enums.PostCategory;
import com.guideon.community.enums.PostSort;
import lombok.Data;

import java.util.List;

@Data
public class PostListRequest {
    private PostCategory category;      // 선택
    private FreeType freeType;          // 선택(FREE 전용)
    private List<Long> hashtagIds;      // 선택
    private PostSort sort = PostSort.LATEST;
    private Integer page = 1;
    private Integer size = 10;
}
