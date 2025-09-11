package com.guideon.plan.ai.prompt;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideon.plan.domain.SectionVO;

import java.util.List;

public class PlanAiPromptBuilder {
    private static final ObjectMapper OM = new ObjectMapper();

    public static String buildSystemPrompt() {
        return String.join("\n",
                "역할: 당신은 소상공인 대출 심사 보조 평가관입니다.",
                "목표: 첨부된 '사업계획서(PDF)'만 근거로 섹션별 가중 득점과 코멘트·개선제안을 산출합니다.",
                "원칙: 근거 불명확 시 보수적으로 감점. 수치·합계·비율 일치 여부는 엄격히 검증.",
                "출력: 반드시 지정 JSON 스키마로만 출력(기타 텍스트 출력 금지)."
        );
    }

    public static String buildUserPrompt(List<SectionVO> masters) {
        try {
            String mastersJson = OM.writeValueAsString(masters); // sectionId, displayLabel, weight, pointsTemplate, formMappings
            return "다음은 섹션 마스터입니다.\n" +
                    "\n\n[섹션 마스터(JSON)]\n" + mastersJson +
                    "\n\n지시사항:\n" +
                    "1) 각 섹션(sectionId)의 weight 범위 내에서 scorePoints를 산출하세요.\n" +
                    "2) totalScore는 모든 섹션 scorePoints 합입니다(0~100).\n" +
                    "3) 각 섹션 comment는 1~2문장, suggestions는 최대 3개로 간결하게.\n" +
                    "4) strengths/risks는 문서 전반 기준으로 최대 5개씩 도출.\n" +
                    "5) JSON 외 불필요한 텍스트는 출력하지 마세요.";
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
