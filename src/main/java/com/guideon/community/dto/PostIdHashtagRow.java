package com.guideon.community.dto;

import com.guideon.community.enums.HashtagType;
import lombok.Data;

@Data
public class PostIdHashtagRow {
    private Long postId;
    private Long id;
    private String name;
    private HashtagType tagType;
    private String code;
}
