package com.guideon.community.dto;

import com.guideon.community.enums.HashtagType;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class HashtagDto {
    private Long id;
    private String name;
    private HashtagType tagType;
    private String code;
}
