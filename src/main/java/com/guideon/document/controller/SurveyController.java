package com.guideon.document.controller;

import com.guideon.common.redis.RedisService;
import com.guideon.document.dto.BusinessInfoDTO;
import com.guideon.document.dto.UserSurveyRequest;
import com.guideon.document.service.SurveyService;
import com.guideon.security.util.LoginUserProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@Log4j2
@RequiredArgsConstructor
@RequestMapping("/api/survey")
public class SurveyController {

    private final SurveyService surveyService;
    private final LoginUserProvider loginUserProvider;

    /**
     * 로그인한 사용자 정보 추출 및 검증
     */
    private Map<String, Object> extractAuthInfo() {
        Long memberId = loginUserProvider.getLoginMemberId();
        String email = loginUserProvider.getLoginEmail();


        if (memberId == null || email == null) {
            throw new SecurityException("인증이 필요합니다. 로그인 후 다시 시도해주세요.");
        }

        Map<String, Object> authInfo = new HashMap<>();
        authInfo.put("memberId", memberId);
        authInfo.put("email", email);
        return authInfo;
    }


    /**
     * 사용자 설문 응답 저장
     */
    @PostMapping("/submit")
    public ResponseEntity<Map<String, Object>> submitSurvey(
           @RequestBody UserSurveyRequest request) {

        try {
            // 1. JWT에서 memberId 추출
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");

            // 2. 회원 정보에서 industryCode 조회 (현재는 임시값)
            String industryCode = "56111";

            // 3. BusinessInfoDTO 생성
            BusinessInfoDTO businessInfoDTO = BusinessInfoDTO.builder()
                    .memberId(memberId)
                    .loanPurpose(request.getLoanPurpose())
                    .industryCode(industryCode)
                    .businessPeriod(request.getBusinessPeriod())
                    .revenue(request.getRevenue())
                    .employees(request.getEmployees())
                    .placeType(request.getPlaceType())
                    .build();

            // 4. 설문 응답 저장
            Long businessId = surveyService.saveUserSurvey(businessInfoDTO);

            // 5. 성공 응답
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "설문 응답이 저장되었습니다.");
            response.put("businessId", businessId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            // 6. 에러 응답
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "설문 응답 저장 중 오류가 발생했습니다: " + e.getMessage());

            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 설문 상태 확인 API
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getSurveyStatus() {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");

            // 설문 완료 상태 확인
            BusinessInfoDTO businessInfo = surveyService.getBusinessInfoByMemberId(memberId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);

            if (businessInfo != null && businessInfo.getSurveyCompletedAt() != null) {
                // 설문이 완료된 경우
                response.put("isCompleted", true);
                response.put("businessId", businessInfo.getBusinessId());
                response.put("surveyData", businessInfo);
            } else {
                // 설문이 미완료인 경우
                response.put("isCompleted", false);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "설문 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }

    /**
     * 새로운 시뮬레이션 시작 (설문 초기화)
     */
    @PostMapping("/reset")
    public ResponseEntity<Map<String, Object>> resetSurvey() {
        try {
            Map<String, Object> authInfo = extractAuthInfo();
            Long memberId = (Long) authInfo.get("memberId");

            // 설문 초기화
            surveyService.resetSurveyByMemberId(memberId);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.put("message", "새로운 시뮬레이션을 시작할 수 있습니다.");

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("success", false);
            errorResponse.put("message", "설문 초기화 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(errorResponse);
        }
    }
}
