package com.guideon.funds.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.funds.domain.SupportAnnouncement;
import com.guideon.funds.dto.SupportAnnouncementDetailResponseDTO;
import com.guideon.funds.dto.SupportAnnouncementListResponseDTO;
import com.guideon.funds.service.SupportAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 소상공인 지원사업 공고 컨트롤러
 */
@RestController
@RequestMapping("/api/support-announcements")
@Api(tags = "소상공인 지원사업 공고 API", description = "소상공인 지원사업 공고 관련 API")
public class SupportAnnouncementController {

    @Autowired
    private SupportAnnouncementService supportAnnouncementService;
    
    /**
     * 전체 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("")
    @ResponseBody
    @ApiOperation(value = "전체 소상공인 지원사업 공고 목록 조회", notes = "모든 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAllAnnouncements() {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAllAnnouncements();
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 지역별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/by-region")
    @ResponseBody
    @ApiOperation(value = "지역별 소상공인 지원사업 공고 목록 조회", notes = "특정 지역의 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByRegion(
            @ApiParam(value = "지역명", required = true) @RequestParam String region) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByRegion(region);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("지역별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("지역별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 업종별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/by-industry")
    @ResponseBody
    @ApiOperation(value = "업종별 소상공인 지원사업 공고 목록 조회", notes = "특정 업종의 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByIndustry(
            @ApiParam(value = "업종명", required = true) @RequestParam String industry) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByIndustry(industry);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("업종별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("업종별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 지역 + 업종별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/filter")
    @ResponseBody
    @ApiOperation(value = "지역+업종별 소상공인 지원사업 공고 목록 조회", notes = "지역과 업종을 모두 만족하는 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByRegionAndIndustry(
            @ApiParam(value = "지역명", required = true) @RequestParam String region,
            @ApiParam(value = "업종명", required = true) @RequestParam String industry) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByRegionAndIndustry(region, industry);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("지역+업종별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("지역+업종별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 신청 상태별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/by-status")
    @ResponseBody
    @ApiOperation(value = "신청 상태별 소상공인 지원사업 공고 목록 조회", notes = "신청 상태(신청가능, 마감 등)별 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByApplyStatus(
            @ApiParam(value = "신청 상태", required = true) @RequestParam String applyStatus) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByApplyStatus(applyStatus);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("신청 상태별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("신청 상태별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 키워드로 소상공인 지원사업 공고 검색 API
     */
    @GetMapping("/search")
    @ResponseBody
    @ApiOperation(value = "키워드로 소상공인 지원사업 공고 검색", notes = "제목, 공고명, 내용, 태그에서 키워드를 검색하여 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> searchAnnouncements(
            @ApiParam(value = "검색 키워드", required = true) @RequestParam String keyword) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.searchAnnouncements(keyword);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("키워드 검색 결과 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("키워드 검색 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 기관별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/by-agency")
    @ResponseBody
    @ApiOperation(value = "기관별 소상공인 지원사업 공고 목록 조회", notes = "특정 기관의 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByAgency(
            @ApiParam(value = "기관명", required = true) @RequestParam String agency) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByAgency(agency);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("기관별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("기관별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 카테고리별 소상공인 지원사업 공고 목록 조회 API
     */
    @GetMapping("/by-category")
    @ResponseBody
    @ApiOperation(value = "카테고리별 소상공인 지원사업 공고 목록 조회", notes = "특정 카테고리의 소상공인 지원사업 공고 목록을 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementListResponseDTO>> getAnnouncementsByCategory(
            @ApiParam(value = "카테고리", required = true) @RequestParam String category) {
        try {
            List<SupportAnnouncement> announcements = supportAnnouncementService.getAnnouncementsByCategory(category);
            SupportAnnouncementListResponseDTO responseData = new SupportAnnouncementListResponseDTO(announcements);
            
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.success("카테고리별 소상공인 지원사업 공고 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementListResponseDTO> response =
                CommonResponseDTO.error("카테고리별 공고 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 특정 ID로 소상공인 지원사업 공고 상세 조회 API
     * 주의: 이 매핑은 다른 구체적인 매핑들 이후에 위치해야 함
     */
    @GetMapping("/{id}")
    @ResponseBody
    @ApiOperation(value = "소상공인 지원사업 공고 상세 조회", notes = "특정 ID의 소상공인 지원사업 공고 상세 정보를 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportAnnouncementDetailResponseDTO>> getAnnouncementById(
            @ApiParam(value = "공고 ID", required = true) @PathVariable Long id) {
        try {
            SupportAnnouncement announcement = supportAnnouncementService.getAnnouncementById(id);
            
            if (announcement == null) {
                CommonResponseDTO<SupportAnnouncementDetailResponseDTO> response =
                    CommonResponseDTO.error("해당 ID의 공고를 찾을 수 없습니다.", HttpStatus.NOT_FOUND.value());
                
                return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
            }
            
            SupportAnnouncementDetailResponseDTO responseData = new SupportAnnouncementDetailResponseDTO(announcement);
            
            CommonResponseDTO<SupportAnnouncementDetailResponseDTO> response =
                CommonResponseDTO.success("소상공인 지원사업 공고 상세 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportAnnouncementDetailResponseDTO> response =
                CommonResponseDTO.error("공고 상세 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
