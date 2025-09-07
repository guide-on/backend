package com.guideon.funds.controller;

import com.guideon.common.dto.CommonResponseDTO;
import com.guideon.funds.domain.SupportCenter;
import com.guideon.funds.dto.CoordinateUpdateResponseDTO;
import com.guideon.funds.dto.NearestCenterRequestDTO;
import com.guideon.funds.dto.NearestCenterResponseDTO;
import com.guideon.funds.dto.SupportCenterListResponseDTO;
import com.guideon.funds.service.CoordinateUpdateService;
import com.guideon.funds.service.NearestCenterService;
import com.guideon.funds.service.SupportCenterService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 지원센터 컨트롤러
 */
@RestController
@RequestMapping("/api/support-centers")
@Api(tags = "지원센터 API", description = "지원센터 관련 API")
public class SupportCenterController {

    @Autowired
    private SupportCenterService supportCenterService;
    
    @Autowired
    private CoordinateUpdateService coordinateUpdateService;
    
    @Autowired
    private NearestCenterService nearestCenterService;
    
    /**
     * 전체 지원센터 목록 조회 API
     * 프론트엔드 드롭다운에서 사용할 전체 센터 목록
     */
    @ResponseBody
    @ApiOperation(value = "전체 지원센터 목록 조회", notes = "모든 지원센터의 목록을 조회합니다. 프론트엔드 드롭다운에서 사용됩니다.")
    @GetMapping("")
    public ResponseEntity<CommonResponseDTO<SupportCenterListResponseDTO>> getAllCenters() {
        try {
            List<SupportCenter> centers = supportCenterService.getAllCenters();
            SupportCenterListResponseDTO responseData = new SupportCenterListResponseDTO(centers);

            CommonResponseDTO<SupportCenterListResponseDTO> response =
                CommonResponseDTO.success("전체 지원센터 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            
            CommonResponseDTO<SupportCenterListResponseDTO> response =
                CommonResponseDTO.error("지원센터 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 좌표가 있는 지원센터만 조회 API
     * 지도에 표시할 센터들만 필요할 때 사용
     */
    @RequestMapping(value = "/with-coordinates", method = RequestMethod.GET)
    @ResponseBody
    @ApiOperation(value = "좌표가 있는 지원센터 목록 조회", notes = "lat, lng 값이 있는 지원센터만 조회합니다.")
    public ResponseEntity<CommonResponseDTO<SupportCenterListResponseDTO>> getCentersWithCoordinates() {
        
        try {
            List<SupportCenter> centers = supportCenterService.getCentersWithCoordinates();
            SupportCenterListResponseDTO responseData = new SupportCenterListResponseDTO(centers);

            CommonResponseDTO<SupportCenterListResponseDTO> response =
                CommonResponseDTO.success("좌표가 있는 지원센터 목록 조회 성공", responseData);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            CommonResponseDTO<SupportCenterListResponseDTO> response =
                CommonResponseDTO.error("지원센터 목록 조회 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 특정 지원센터 좌표 업데이트 API
     */
    @RequestMapping(value = "/{centerId}/coordinates", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "지원센터 좌표 업데이트", notes = "카카오 Local API를 사용하여 특정 지원센터의 좌표를 업데이트합니다.")
    public ResponseEntity<CommonResponseDTO<CoordinateUpdateResponseDTO>> updateCenterCoordinates(@PathVariable Long centerId) {
        
        try {
            CoordinateUpdateResponseDTO result = coordinateUpdateService.updateCenterCoordinates(centerId);
            
            if (result.isUpdated()) {
                CommonResponseDTO<CoordinateUpdateResponseDTO> response = 
                    CommonResponseDTO.success("지원센터 좌표 업데이트 성공", result);
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                CommonResponseDTO<CoordinateUpdateResponseDTO> response = 
                    CommonResponseDTO.error("지원센터 좌표 업데이트 실패: " + result.getErrorMessage(), HttpStatus.BAD_REQUEST.value());
                
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            
            CommonResponseDTO<CoordinateUpdateResponseDTO> response = 
                CommonResponseDTO.error("좌표 업데이트 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 모든 지원센터 좌표 일괄 업데이트 API
     */
    @RequestMapping(value = "/coordinates/batch-update", method = RequestMethod.PUT)
    @ResponseBody
    @ApiOperation(value = "모든 지원센터 좌표 일괄 업데이트", notes = "카카오 Local API를 사용하여 모든 지원센터의 좌표를 일괄 업데이트합니다.")
    public ResponseEntity<CommonResponseDTO<List<CoordinateUpdateResponseDTO>>> updateAllCentersCoordinates() {
        
        try {
            List<CoordinateUpdateResponseDTO> results = coordinateUpdateService.updateAllCentersCoordinates();
            
            long successCount = results.stream().mapToLong(r -> r.isUpdated() ? 1 : 0).sum();
            long totalCount = results.size();
            
            String message = String.format("좌표 일괄 업데이트 완료 - 성공: %d개, 전체: %d개", successCount, totalCount);
            
            CommonResponseDTO<List<CoordinateUpdateResponseDTO>> response = 
                CommonResponseDTO.success(message, results);
            
            return new ResponseEntity<>(response, HttpStatus.OK);
            
        } catch (Exception e) {
            
            CommonResponseDTO<List<CoordinateUpdateResponseDTO>> response = 
                CommonResponseDTO.error("일괄 좌표 업데이트 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    /**
     * 사업장 주소 기반 가장 가까운 센터 찾기 API
     */
    @RequestMapping(value = "/nearest", method = RequestMethod.POST)
    @ResponseBody
    @ApiOperation(value = "가장 가까운 지원센터 찾기", notes = "사업장 주소를 기반으로 가장 가까운 지원센터를 찾습니다.")
    public ResponseEntity<CommonResponseDTO<NearestCenterResponseDTO>> findNearestCenters(@RequestBody NearestCenterRequestDTO request) {
        
        try {
            NearestCenterResponseDTO result = nearestCenterService.findNearestCenters(request);
            
            if (result.isSuccess()) {
                String message = String.format("가까운 지원센터 %d개 조회 성공", 
                                              result.getNearestCenters() != null ? result.getNearestCenters().size() : 0);
                
                CommonResponseDTO<NearestCenterResponseDTO> response = 
                    CommonResponseDTO.success(message, result);
                
                return new ResponseEntity<>(response, HttpStatus.OK);
            } else {
                CommonResponseDTO<NearestCenterResponseDTO> response = 
                    CommonResponseDTO.error("가까운 센터 찾기 실패: " + result.getErrorMessage(), HttpStatus.BAD_REQUEST.value());
                
                return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
            }
            
        } catch (Exception e) {
            
            CommonResponseDTO<NearestCenterResponseDTO> response = 
                CommonResponseDTO.error("가까운 센터 찾기 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR.value());
            
            return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
