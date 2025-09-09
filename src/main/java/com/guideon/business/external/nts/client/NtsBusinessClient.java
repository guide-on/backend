package com.guideon.business.external.nts.client;

import com.guideon.business.external.nts.dto.BusinessStatusRequest;
import com.guideon.business.external.nts.dto.BusinessStatusResponse;
import com.guideon.business.external.nts.exception.NtsApiException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;

@Slf4j
@Component
@RequiredArgsConstructor
public class NtsBusinessClient {
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${odcloud.serviceKey.urlEncoded}") // URL 인코딩된 서비스키 권장
    private String serviceKey;

    @Value("${odcloud.baseUrl:https://api.odcloud.kr/api}")
    private String baseUrl;

    /**
     * 국세청 사업자 '상태조회' API 호출 (최대 100건/회)
     * @param req body: { "b_no": ["1234567890", ...] }
     */
    public BusinessStatusResponse fetchStatus(BusinessStatusRequest req) {
        String url = baseUrl + "/nts-businessman/v1/status";
        URI uri = URI.create(url);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(java.util.List.of(MediaType.APPLICATION_JSON));

        // Authorization 헤더에 serviceKey 넣기
        headers.set("Authorization", "Infuser " + serviceKey);

        HttpEntity<BusinessStatusRequest> entity = new HttpEntity<>(req, headers);

        ResponseEntity<BusinessStatusResponse> resp;
        try {
            resp = restTemplate.exchange(uri, HttpMethod.POST, entity, BusinessStatusResponse.class);
        } catch (Exception e) {
            log.error(e.getMessage());
            log.info(url);
            throw NtsApiException.httpError("HTTP_ERROR", e);
        }

        if (!resp.getStatusCode().is2xxSuccessful() || resp.getBody() == null) {
            throw NtsApiException.apiError("INTERNAL_ERROR", "응답이 비정상입니다.");
        }

        // API 자체 status_code가 OK가 아닐 수 있음 → 예외 변환
        if (!"OK".equalsIgnoreCase(resp.getBody().getStatus_code())) {
            throw NtsApiException.apiError(resp.getBody().getStatus_code(),
                    "NTS API 오류(status_code=" + resp.getBody().getStatus_code() + ")");
        }

        return resp.getBody();
    }
}
