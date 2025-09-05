package com.guideon.document.controller;

import com.guideon.document.dto.PolicyDTO;
import com.guideon.document.service.PolicyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/policy")
@Log4j2
public class PolicyController {

    private final PolicyService policyService;

    /**
     * 맞춤형 정책자금 목록 조회
     * 1. 소상공인 자격 판별
     * 2. 자격 충족 시 정책자금 필터링 후 목록 반환
     * 3. 자격 미달 시 소상공인 아님 메시지 반환
     */
    @GetMapping("/{businessId}")
    public ResponseEntity<Map<String, Object>> getPolicies(@PathVariable Long businessId) {
        try {
            log.info("정책자금 조회 요청: businessId={}", businessId);

            // 1. 소상공인 자격 판별
            if (!policyService.isSmallBusiness(businessId)) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("isSmallBusiness", false);
                response.put("message", "소상공인 자격 요건에 해당하지 않습니다.");

                return ResponseEntity.ok(response);
            }

            // 2. 정책자금 필터링
            List<PolicyDTO> eligiblePolicies = policyService.filterEligiblePolicies(businessId);

            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("isSmallBusiness", true);
            response.put("eligiblePolicies", eligiblePolicies);
            response.put("totalCount", eligiblePolicies.size());

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("정책자금 조회 오류: businessId={}", businessId, e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }
}
