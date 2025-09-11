package com.guideon.plan.service;

import com.guideon.document.domain.DocumentUploadsVO;
import com.guideon.document.service.DocumentService;
import com.guideon.plan.domain.*;
import com.guideon.plan.dto.EvaluationResultDTO;
import com.guideon.plan.dto.SectionDetailDTO;
import com.guideon.plan.mapper.PlanEvalMapper;
import com.guideon.plan.parser.PdfPlanExtractor;
import com.guideon.plan.policy.GradePolicy;
import com.guideon.plan.rule.PlanEvalRules;
import com.guideon.security.util.LoginUserProvider;
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
    private final PlanEvalMapper mapper;
    private final DocumentService documentService;
    private final LoginUserProvider loginUserProvider;

    private static final String GROUP_BUSINESS_PLAN = "business_plan";

    /** 업로드된 document_uploads.id를 받아 평가하고 보고서/섹션 결과 저장 */
    @Transactional
    public PdfPlanExtractor evaluateDocument(Long sessionId) throws Exception {
        Long memberId = loginUserProvider.getLoginMemberId();

        // 문서 조회 (파일 경로 필수)
        DocumentUploadsVO doc = documentService.getDocument(sessionId, GROUP_BUSINESS_PLAN);
        if (doc == null) throw new IllegalArgumentException("document not found");
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

        return fx;
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
