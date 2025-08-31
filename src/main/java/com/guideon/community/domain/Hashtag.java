package com.guideon.community.domain;

import com.guideon.community.enums.HashtagType;
import lombok.Data;
import java.sql.Timestamp;

@Data
public class Hashtag {
    private Long id;
    private String name;
    private HashtagType tagType;
    private String code;
    private String refKsicCode;
    private Boolean isActive;
    private Timestamp createdAt;
    private Timestamp updatedAt;
}
