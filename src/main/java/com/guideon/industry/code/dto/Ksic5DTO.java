package com.guideon.industry.code.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ksic5DTO {
    private String code; // 5자리 KSIC
    private String name;  // 업종명(한글)
}
