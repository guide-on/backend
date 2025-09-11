package com.guideon.plan.rule;

import com.guideon.plan.domain.SectionResultVO;
import com.guideon.plan.domain.SectionVO;
import com.guideon.plan.parser.PdfPlanExtractor;
import lombok.*;

import java.util.*;

@Getter
@AllArgsConstructor
public class PlanEvalRules {
    /** plan_eval_section 을 section_id ASC로 받은 순서대로 규칙 적용 */
    public List<SectionResultVO> evaluate(PdfPlanExtractor fx, List<SectionVO> masters) {
        List<SectionResultVO> out = new ArrayList<>();
        for (int i = 0; i < masters.size(); i++) {
            SectionVO m = masters.get(i);
            SectionResultVO s;
            switch (i) {
                case 0:  s = scoreS1(fx, m);  break;
                case 1:  s = scoreS2(fx, m);  break;
                case 2:  s = scoreS3(fx, m);  break;
                case 3:  s = scoreS4(fx, m);  break;
                case 4:  s = scoreS5(fx, m);  break;
                case 5:  s = scoreS6(fx, m);  break;
                case 6:  s = scoreS7(fx, m);  break;
                case 7:  s = scoreS8(fx, m);  break;
                case 8:  s = scoreS9(fx, m);  break;
                case 9:  s = scoreS10(fx, m); break;
                default: s = scoreGeneric(fx, m);
            }
            out.add(s);
        }
        return out;
    }

    // ===== 규칙들 (내부 비율 * weight → score) =====
    private SectionResultVO scoreS1(PdfPlanExtractor fx, SectionVO m) {
        int filled=0,total=5;
        if (nz(fx.getSalesAmountMn())) filled++;
        if (nz(fx.getExportAmountMn())) filled++;
        if (nz(fx.getManuPercent())) filled++;
        if (nz(fx.getOutsourcePercent())) filled++;
        if (sum100(fx.getOrderPercent(), fx.getDirectPercent())) filled++;
        double ratio = (double)filled/total;
        return build(m, ratio, "영업현황 기본 수치 기재 정도", sg(
                sum100(fx.getManuPercent(), fx.getOutsourcePercent()) ? null : "자사제조/외주가공 합계 100% 점검",
                sum100(fx.getOrderPercent(), fx.getDirectPercent()) ? null : "주문/직접 판매비중 합계 100% 점검"
        ));
    }
    private SectionResultVO scoreS2(PdfPlanExtractor fx, SectionVO m){
        int filled=0,total=3;
        if (nz(fx.getSalesAmountMn())) filled++;
        if (nz(fx.getManuPercent()) || nz(fx.getOutsourcePercent())) filled++;
        if (nz(fx.getTopClientRatio())) filled++;
        double ratio=(double)filled/total;
        return build(m, ratio, "제품/서비스 정의의 구체성(간이)", null);
    }
    private SectionResultVO scoreS3(PdfPlanExtractor fx, SectionVO m){
        int filled=0,total=2;
        if (nz(fx.getWorkDaysPerMonth())) filled++;
        if (nz(fx.getHoursPerDay())) filled++;
        double ratio=(double)filled/total;
        return build(m, ratio, "경영 관련 기본 체계 기재 여부(간이)", null);
    }
    private SectionResultVO scoreS4(PdfPlanExtractor fx, SectionVO m){
        Integer top=fx.getTopClientRatio();
        double ratio=(top==null)?0.5:(top<=50?1.0:(top<=70?0.7:0.4));
        return build(m, ratio, "상위 거래처 집중도 기반 안정성",
                sg(top!=null && top>60? "상위 거래처 집중도 완화 계획 제시":null));
    }
    private SectionResultVO scoreS5(PdfPlanExtractor fx, SectionVO m){
        int ok=0,total=2;
        if (inRange(fx.getWorkDaysPerMonth(),1,31)) ok++;
        if (inRange(fx.getHoursPerDay(),1,24)) ok++;
        double ratio=(double)ok/total;
        return build(m, ratio, "가동상황의 현실성", sg(
                inRange(fx.getWorkDaysPerMonth(),1,31)?null:"월평균 영업일 범위(1~31) 재확인",
                inRange(fx.getHoursPerDay(),1,24)?null:"1일 평균 시간 범위(1~24) 재확인"));
    }
    private SectionResultVO scoreS6(PdfPlanExtractor fx, SectionVO m){
        int filled=0,total=2;
        if (nz(fx.getFundTotalMn())) filled++;
        if (nz(fx.getPlanTotalMn())) filled++;
        double ratio=(double)filled/total;
        return build(m, ratio, "향후 계획 기본 재원 연계 여부", null);
    }
    private SectionResultVO scoreS7(PdfPlanExtractor fx, SectionVO m){
        int pts=0,total=3;
        if (nz(fx.getFundFacilityMn())) pts++;
        if (nz(fx.getFundWorkingMn())) pts++;
        if (sumEq(fx.getFundFacilityMn(), fx.getFundWorkingMn(), fx.getFundTotalMn())) pts++;
        double ratio=(double)pts/total;
        return build(m, ratio, "자금용도 구체성 및 합계 일치",
                sg(sumEq(fx.getFundFacilityMn(), fx.getFundWorkingMn(), fx.getFundTotalMn())?null:"자금소요(시설+운전=합계) 일치 검증"));
    }
    private SectionResultVO scoreS8(PdfPlanExtractor fx, SectionVO m){
        int pts=0,total=2;
        if (nz(fx.getFundTotalMn())) pts++;
        if (sumEq(fx.getFundFacilityMn(), fx.getFundWorkingMn(), fx.getFundTotalMn())) pts++;
        double ratio=(double)pts/total;
        return build(m, ratio, "자금소요 금액 산출 근거(간이)", null);
    }
    private SectionResultVO scoreS9(PdfPlanExtractor fx, SectionVO m){
        int pts=0,total=2;
        if (nz(fx.getPlanTotalMn())) pts++;
        if (sumEq(fx.getPlanLoanMn(), fx.getPlanSelfMn(), fx.getPlanBankEtcMn(), fx.getPlanTotalMn())) pts++;
        double ratio=(double)pts/total;
        return build(m, ratio, "자금조달 합계 일치 여부",
                sg(sumEq(fx.getPlanLoanMn(), fx.getPlanSelfMn(), fx.getPlanBankEtcMn(), fx.getPlanTotalMn())?null:"자금조달 항목 합계가 총액과 일치하도록 재검토"));
    }
    private SectionResultVO scoreS10(PdfPlanExtractor fx, SectionVO m){
        int ok=0,total=3;
        if (sum100(fx.getManuPercent(), fx.getOutsourcePercent())) ok++;
        if (sum100(fx.getOrderPercent(), fx.getDirectPercent())) ok++;
        if (nz(fx.getSalesAmountMn()) && fx.getSalesAmountMn()>=0) ok++;
        double ratio=(double)ok/total;
        return build(m, ratio, "핵심 수치의 일관성", null);
    }
    private SectionResultVO scoreGeneric(PdfPlanExtractor fx, SectionVO m){
        return build(m, 0.5, "기본 규칙(임시)", Collections.emptyList());
    }

    private SectionResultVO build(SectionVO m, double ratio, String comment, List<String> suggestions){
        double weight = m.getWeight()==null?0.0:m.getWeight();
        double r = Math.max(0.0, Math.min(1.0, ratio));
        double score = Math.round(r * weight * 100.0)/100.0;
        return SectionResultVO.builder()
                .sectionId(m.getSectionId())
                .label(m.getDisplayLabel())
                .weight(weight)
                .score(score)
                .comment(comment)
                .suggestions(suggestions==null? new ArrayList<>() : suggestions)
                .points(m.getPointsTemplate())
                .mappings(m.getFormMappings())
                .build();
    }

    private static boolean nz(Number n){ return n!=null; }
    private static boolean inRange(Integer v,int min,int max){ return v!=null && v>=min && v<=max; }
    private static boolean sum100(Integer a,Integer b){ return a!=null && b!=null && a+b==100; }
    private static boolean sumEq(Double a,Double b,Double total){ if(a==null||b==null||total==null) return false; return Math.abs((a+b)-total)<0.0001; }
    private static boolean sumEq(Double a,Double b,Double c, Double total){ if(a==null||b==null||c==null||total==null) return false; return Math.abs((a+b+c)-total)<0.0001; }
    private static List<String> sg(String... msgs){ List<String> out=new ArrayList<>(); if(msgs==null) return out; for(String m:msgs) if(m!=null && !m.isBlank()) out.add(m); return out; }
}
