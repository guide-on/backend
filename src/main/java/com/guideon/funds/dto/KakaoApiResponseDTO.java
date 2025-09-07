package com.guideon.funds.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 카카오 API 응답 DTO
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonIgnoreProperties(ignoreUnknown = true)
@ApiModel(description = "카카오 API 응답")
public class KakaoApiResponseDTO {
    
    @ApiModelProperty(value = "검색 결과 문서")
    private List<Document> documents;
    
    @ApiModelProperty(value = "검색 메타 정보")
    private Meta meta;
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "검색 결과 문서")
    public static class Document {
        
        @ApiModelProperty(value = "주소명", example = "서울 중구 세종대로 110")
        private String address_name;
        
        @ApiModelProperty(value = "주소 타입", example = "ROAD_ADDR")
        private String address_type;
        
        @ApiModelProperty(value = "X 좌표값 (경도)", example = "126.97794838463647")
        private String x;
        
        @ApiModelProperty(value = "Y 좌표값 (위도)", example = "37.56673456781234")
        private String y;
        
        @ApiModelProperty(value = "지번 주소 정보")
        private Address address;
        
        @ApiModelProperty(value = "도로명 주소 정보")
        private RoadAddress road_address;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "지번 주소 정보")
    public static class Address {
        
        @ApiModelProperty(value = "지번 주소", example = "서울 중구 태평로1가 31")
        private String address_name;
        
        @ApiModelProperty(value = "1단계 지역명 (시도)", example = "서울특별시")
        private String region_1depth_name;
        
        @ApiModelProperty(value = "2단계 지역명 (시군구)", example = "중구")
        private String region_2depth_name;
        
        @ApiModelProperty(value = "3단계 지역명 (동)", example = "태평로1가")
        private String region_3depth_name;
        
        @ApiModelProperty(value = "3단계 지역명 (행정동)", example = "명동")
        private String region_3depth_h_name;
        
        @ApiModelProperty(value = "행정구역코드", example = "1114010100")
        private String h_code;
        
        @ApiModelProperty(value = "법정구역코드", example = "1114010100")
        private String b_code;
        
        @ApiModelProperty(value = "산 여부", example = "N")
        private String mountain_yn;
        
        @ApiModelProperty(value = "지번 주번지", example = "31")
        private String main_address_no;
        
        @ApiModelProperty(value = "지번 부번지", example = "")
        private String sub_address_no;
        
        @ApiModelProperty(value = "X 좌표값 (경도)", example = "126.97794838463647")
        private String x;
        
        @ApiModelProperty(value = "Y 좌표값 (위도)", example = "37.56673456781234")
        private String y;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "도로명 주소 정보")
    public static class RoadAddress {
        
        @ApiModelProperty(value = "도로명 주소", example = "서울 중구 세종대로 110")
        private String address_name;
        
        @ApiModelProperty(value = "건물명", example = "서울특별시청")
        private String building_name;
        
        @ApiModelProperty(value = "1단계 지역명 (시도)", example = "서울특별시")
        private String region_1depth_name;
        
        @ApiModelProperty(value = "2단계 지역명 (시군구)", example = "중구")
        private String region_2depth_name;
        
        @ApiModelProperty(value = "3단계 지역명 (동)", example = "소공동")
        private String region_3depth_name;
        
        @ApiModelProperty(value = "도로명", example = "세종대로")
        private String road_name;
        
        @ApiModelProperty(value = "지하 여부", example = "N")
        private String underground_yn;
        
        @ApiModelProperty(value = "건물 주번지", example = "110")
        private String main_building_no;
        
        @ApiModelProperty(value = "건물 부번지", example = "")
        private String sub_building_no;
        
        @ApiModelProperty(value = "우편번호", example = "04524")
        private String zone_no;
        
        @ApiModelProperty(value = "X 좌표값 (경도)", example = "126.97794838463647")
        private String x;
        
        @ApiModelProperty(value = "Y 좌표값 (위도)", example = "37.56673456781234")
        private String y;
    }
    
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @JsonIgnoreProperties(ignoreUnknown = true)
    @ApiModel(description = "검색 메타 정보")
    public static class Meta {
        
        @ApiModelProperty(value = "검색된 문서 수", example = "1")
        private int total_count;
        
        @ApiModelProperty(value = "현재 페이지에 노출된 문서 수", example = "1")
        private int pageable_count;
        
        @ApiModelProperty(value = "마지막 페이지 여부", example = "true")
        private boolean is_end;
    }
}
