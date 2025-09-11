package com.guideon.plan.mapper;

import com.guideon.plan.domain.*;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface PlanEvalMapper {
    // 섹션 마스터
    List<SectionVO> selectSection();

    // 평가 리포트/섹션
    int insertReport(PlanEvalReportVO vo);
    int insertSectionResult(PlanEvalSectionResultVO vo);

    // 조회
    ReportHeaderVO selectReportHeader(@Param("sessionId") Long sessionId);
    List<SectionResultVO> selectSectionResults(@Param("reportId") Long reportId);
}
