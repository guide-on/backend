package com.guideon.industry.catalog.domain;

import lombok.Data;

import java.util.List;

@Data
public class IndustryTag {
    private String id;
    private String label;
    private List<String> codes; // 2자리 KSIC 배열
}
