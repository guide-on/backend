package com.guideon.funds.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * 지원센터
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SupportCenter {
    private Long id;
    private String name;
    private String jurisdiction;
    private String address;
    private String phone;
    private String fax;
    private Double lat;
    private Double lng;
    private Date createdAt;
    private Date updatedAt;

}
