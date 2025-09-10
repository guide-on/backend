package com.guideon.document.controller;

import com.guideon.document.dto.SessionRequest;
import com.guideon.document.service.LoanSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/session")
@Log4j2
public class LoanSessionController {

    private final LoanSessionService loanSessionService;

    /**
     * 대출 세션 생성
     * 정책자금 선택 시 호출되어 새로운 대출 세션을 시작합니다.
     */
    @PostMapping("/create")
    public ResponseEntity<Map<String, Object>> createLoanSession(
            @RequestBody SessionRequest request) {

        try {
            log.info("대출 세션 생성 요청: businessId={}, policyId={}",
                    request.getBusinessId(), request.getPolicyId());

            Map<String, Object> result = loanSessionService.createLoanSession(request);

            Map<String, Object> response = new LinkedHashMap<>();
            response.put("success", true);
            response.putAll(result);

            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            log.error("잘못된 요청: {}", e.getMessage());
            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", e.getMessage()
            ));

        } catch (Exception e) {
            log.error("대출 세션 생성 중 오류 발생", e);
            return ResponseEntity.internalServerError().body(Map.of(
                    "success", false,
                    "message", "서버 오류가 발생했습니다."
            ));
        }
    }
}
