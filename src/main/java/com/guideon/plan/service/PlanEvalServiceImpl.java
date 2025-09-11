package com.guideon.plan.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideon.document.domain.DocumentUploadsVO;
import com.guideon.document.service.DocumentService;
import com.guideon.plan.ai.domain.AiEvalResult;
import com.guideon.plan.ai.prompt.PlanAiPromptBuilder;
import com.guideon.plan.domain.*;
import com.guideon.plan.dto.EvaluationResultDTO;
import com.guideon.plan.dto.SectionDetailDTO;
import com.guideon.plan.mapper.PlanEvalMapper;
import com.guideon.plan.parser.PdfPlanExtractor;
import com.guideon.plan.policy.GradePolicy;
import com.guideon.plan.rule.PlanEvalRules;
import com.guideon.security.util.LoginUserProvider;
import com.guideon.simulation.result.service.SimulationResultService;
import com.openai.client.OpenAIClient;
import com.openai.models.ChatModel;
import com.openai.models.files.FileCreateParams;
import com.openai.models.files.FileObject;
import com.openai.models.files.FilePurpose;
import com.openai.models.responses.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlanEvalServiceImpl implements PlanEvalService{
    private final OpenAIClient openAIClient;
    private final PlanEvalMapper mapper;
    private final DocumentService documentService;
    private final SimulationResultService simulationResultService;
    private final LoginUserProvider loginUserProvider;

    private static final ObjectMapper OM = new ObjectMapper();

    private static final String GROUP_BUSINESS_PLAN = "business_plan";

    /** 업로드된 document_uploads.id를 받아 평가하고 보고서/섹션 결과 저장 */
    @Transactional
    public PdfPlanExtractor evaluateDocument(Long sessionId) throws Exception {
        Long memberId = loginUserProvider.getLoginMemberId();

        // 문서 조회 (파일 경로 필수)
        DocumentUploadsVO doc = documentService.getDocument(sessionId, GROUP_BUSINESS_PLAN);
        if (doc == null) throw new IllegalArgumentException("business_plan 문서를 찾을 수 없습니다. sessionId=" + sessionId);
        if (doc.getFilePath() == null) throw new IllegalStateException("file_path is null for documentId=" + doc.getId());

        Path path = Paths.get(doc.getFilePath());
        if (!Files.exists(path)) throw new IllegalStateException("file not found: " + path);

        // 마스터 로드
        List<SectionVO> masters = mapper.selectSection();

        // PDF 파싱
        PdfPlanExtractor fx = PdfPlanExtractor.from(path.toFile());

        // 규칙 평가 (SectionResultVO 바로 반환)
        List<SectionResultVO> sections = new PlanEvalRules().evaluate(fx, masters);

        // 총점
        double total = sections.stream().mapToDouble(SectionResultVO::getScore).sum();
        total = Math.round(total * 100.0)/100.0;

        // 강점/리스크 간단 규칙 (AI 없이)
        List<String> strengths = new ArrayList<>();
        List<String> risks = new ArrayList<>();
        if (fx.getFundTotalMn()!=null && fx.getPlanTotalMn()!=null) strengths.add("자금소요·조달 수치가 제시됨");
        if (fx.getTopClientRatio()!=null && fx.getTopClientRatio()<=50) strengths.add("거래처 집중도 양호(상위 1개 ≤ 50%)");
        if (!sum100(fx.getManuPercent(), fx.getOutsourcePercent())) risks.add("자사제조/외주가공 비중 합계가 100% 아님");
        if (!sum100(fx.getOrderPercent(), fx.getDirectPercent())) risks.add("주문/직접 판매비중 합계가 100% 아님");
        if (fx.getTopClientRatio()!=null && fx.getTopClientRatio()>70) risks.add("상위 거래처 집중도가 높음");

        // 보고서 저장
        PlanEvalReportVO report = PlanEvalReportVO.builder()
                .memberId(memberId)
                .documentId(doc.getId())
                .sessionId(sessionId)
                .totalScore(total)
                .strengths(strengths)
                .risks(risks)
                .build();
        mapper.insertReport(report);
        Long reportId = report.getReportId();

        // 섹션 저장
        for (SectionResultVO s : sections) {
            PlanEvalSectionResultVO row = PlanEvalSectionResultVO.builder()
                    .reportId(reportId)
                    .sectionId(s.getSectionId())
                    .scorePoints(s.getScore())
                    .comment(s.getComment())
                    .suggestions(s.getSuggestions())
                    .build();
            mapper.insertSectionResult(row);
        }
        simulationResultService.updateSimulationStatus(sessionId, total);

        return fx;
    }

    @Transactional
    public void evaluateSessionWithAI(Long sessionId) throws Exception {
        Long memberId = loginUserProvider.getLoginMemberId();

        // 문서 조회 (파일 경로 필수)
        DocumentUploadsVO doc = documentService.getDocument(sessionId, GROUP_BUSINESS_PLAN);
        if (doc == null) throw new IllegalArgumentException("business_plan 문서를 찾을 수 없습니다. sessionId=" + sessionId);
        if (doc.getFilePath() == null) throw new IllegalStateException("file_path is null for documentId=" + doc.getId());


        Path pdf =Paths.get(doc.getFilePath());
        if (!Files.exists(pdf)) {
            throw new IllegalStateException("파일이 존재하지 않습니다: " + pdf);
        }

        // 1) 섹션 마스터
        List<SectionVO> masters = mapper.selectSection();

        // 2) PDF 업로드 (purpose=INPUT)
        FileObject uploaded = openAIClient.files().create(
                FileCreateParams.builder()
                        .file(pdf)                          // java.nio.file.Path OK (File 아님)
                        .purpose(FilePurpose.ASSISTANTS)         // Responses/Assistants 입력용
                        .build()
        );

        // 3) 프롬프트
        String system = PlanAiPromptBuilder.buildSystemPrompt();
        String userPrompt = PlanAiPromptBuilder.buildUserPrompt(masters);
        //  - userPrompt 안에서 "이 형식의 JSON만 출력" 가이드를 꼭 넣어두세요 (예: code block 금지, 설명문 금지 등)

        // 4) Responses 입력 구성
        List<ResponseInputItem> inputs = List.of(
                ResponseInputItem.ofMessage(
                        ResponseInputItem.Message.builder()
                                .role(ResponseInputItem.Message.Role.SYSTEM)
                                .addInputTextContent(system)
                                .build()
                ),
                ResponseInputItem.ofMessage(
                        ResponseInputItem.Message.builder()
                                .role(ResponseInputItem.Message.Role.USER)
                                .addInputTextContent(userPrompt)
                                // PDF 파일을 메시지에 첨부(모델이 참조할 수 있게)
                                .addContent(
                                        ResponseInputFile.builder()
                                                .fileId(uploaded.id())  // FileObject.id()
                                                .build()
                                )
                                .build()
                )
        );

        // 5) 호출 (단순 텍스트 응답 → JSON 파싱)
        ResponseCreateParams params = ResponseCreateParams.builder()
                .model(ChatModel.GPT_4_1_MINI)                         // 필요시 모델 상향
                .input(ResponseCreateParams.Input.ofResponse(inputs))  // ← 핵심
                .maxOutputTokens(1200)
                .build();

        Response res = openAIClient.responses().create(params);

        // 6) 응답 텍스트 모으기 (content().outputText())
        String raw = res.output().stream()
                .flatMap(o -> o.message().stream())
                .flatMap(m -> m.content().stream())
                .flatMap(c -> c.outputText().stream())
                .map(ResponseOutputText::text)
                .collect(Collectors.joining("\n"));

        // 7) JSON → DTO 역직렬화
        //    모델 프롬프트에서 AiEvalResult 스키마 그대로 출력하도록 강제해야 함
        AiEvalResult ai = OM.readValue(raw, AiEvalResult.class);

        // 총점 반올림
        double total = Math.round(ai.getTotalScore() * 100.0) / 100.0;

        // 8) DB 저장
        PlanEvalReportVO report = PlanEvalReportVO.builder()
                .memberId(memberId)
                .documentId(doc.getId())
                .totalScore(total)
                .strengths(ai.getStrengths())
                .risks(ai.getRisks())
                .build();
        mapper.insertReport(report);
        Long reportId = report.getReportId();

        ai.getSections().forEach(s ->
                mapper.insertSectionResult(
                        PlanEvalSectionResultVO.builder()
                                .reportId(reportId)
                                .sectionId(s.getSectionId())
                                .scorePoints(s.getScorePoints())
                                .comment(s.getComment())
                                .suggestions(s.getSuggestions())
                                .build()
                )
        );
        simulationResultService.updateSimulationStatus(sessionId, total);
    }

    /** 최소 데이터만 반환 */
    public EvaluationResultDTO getByReportId(Long sessionId) {
        ReportHeaderVO head = mapper.selectReportHeader(sessionId);
        List<SectionResultVO> rows = mapper.selectSectionResults(head.getReportId());

        List<SectionDetailDTO> sections = rows.stream().map(r ->
                SectionResultVO.builder()
                        .sectionId(r.getSectionId())
                        .label(r.getLabel())
                        .weight(r.getWeight())
                        .score(r.getScore())
                        .comment(r.getComment())
                        .suggestions(r.getSuggestions())
                        .points(r.getPoints())
                        .mappings(r.getMappings())
                        .build()
        ).map(SectionDetailDTO::from).collect(Collectors.toList());

        return EvaluationResultDTO.builder()
                .reportId(head.getReportId())
                .documentId(head.getDocumentId())
                .totalScore(head.getTotalScore())
                .grade(GradePolicy.toGrade(head.getTotalScore()))
                .strengths(head.getStrengths())
                .risks(head.getRisks())
                .sections(sections)
                .build();
    }

    private static boolean sum100(Integer a,Integer b){ return a!=null && b!=null && a+b==100; }
}
