package com.guideon.document.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideon.document.domain.BusinessInfoVO;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 정책자금별 필요 서류 JSON 파싱 및 처리 서비스
 */
@Service
@Log4j2
public class DocumentParsingService {

    private final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * JSON 파싱해서 서류 그룹 목록 반환
     *
     * @param requiredDocumentsJson policies.required_documents JSON 문자열
     * @param businessInfo 사용자 사업체 정보 (조건 평가용)
     *
     * @return 파싱된 서류 그룹 목록. 각 그룹은 groupKey, label, minSelect, documents 포함
     *
     * @throws RuntimeException JSON 파싱 실패 시
     */
    public List<Map<String, Object>> parseRequiredDocuments(String requiredDocumentsJson, BusinessInfoVO businessInfo) {
        List<Map<String, Object>> documentGroups = new ArrayList<>();

        try {
            if (requiredDocumentsJson == null || requiredDocumentsJson.trim().isEmpty()) {
                log.warn("required_documents JSON이 비어있습니다.");
                return documentGroups;
            }

            // JSON 배열 파싱
            List<Map<String, Object>> rawGroups = objectMapper.readValue(
                    requiredDocumentsJson,
                    new TypeReference<List<Map<String, Object>>>() {}
            );

            // 각 그룹을 처리
            for (Map<String, Object> rawGroup : rawGroups) {
                // trigger 조건 확인
                String trigger = (String) rawGroup.get("trigger");
                if (!evaluateTriggerCondition(trigger, businessInfo)) {
                    continue; // 조건에 맞지 않으면 스킵
                }

                // 그룹 정보 구성
                Map<String, Object> group = new LinkedHashMap<>();
                group.put("groupKey", rawGroup.get("group_key"));
                group.put("label", rawGroup.get("label"));
                group.put("minSelect", rawGroup.get("min_select"));
                group.put("description", rawGroup.get("description"));

                // 서류 목록 처리
                List<Map<String, Object>> docs = (List<Map<String, Object>>) rawGroup.get("docs");
                List<Map<String, Object>> processedDocs = new ArrayList<>();

                if (docs != null) {
                    for (Map<String, Object> doc : docs) {
                        Map<String, Object> processedDoc = new LinkedHashMap<>();
                        processedDoc.put("name", doc.get("name"));
                        processedDoc.put("mydataEligible", doc.get("mydata_eligible"));
                        processedDoc.put("status", "pending"); // 초기 상태
                        processedDocs.add(processedDoc);
                    }
                }

                group.put("documents", processedDocs);
                documentGroups.add(group);
            }

        } catch (Exception e) {
            log.error("JSON 파싱 실패: {}", requiredDocumentsJson, e);
            throw new RuntimeException("서류 목록 파싱 중 오류가 발생했습니다.");
        }

        return documentGroups;
    }

    /**
     * trigger 조건 평가
     */
    public boolean evaluateTriggerCondition(String trigger, BusinessInfoVO businessInfo) {
        if (trigger == null || trigger.trim().isEmpty()) {
            return true; // trigger가 없으면 항상 포함
        }

        // 1. 상시근로자 유뮤 서류 체크
        // "user.employees == 0" 형태의 조건 평가
        if (trigger.contains("user.employees == 0")) {
            return businessInfo.getEmployees() == 0;
        }
        if (trigger.contains("user.employees > 0")) {
            return businessInfo.getEmployees() > 0;
        }

        // 2. 청년 조건
        if (trigger.contains("user.business_period >= 36 OR user.age > 39")) {
            // 현재는 업력만 체크 (나이는 나중에 처리)
            return businessInfo.getBusinessPeriod() >= 36;
        }

        // 기본적으로 true 반환 (알 수 없는 조건)
        return true;
    }
}