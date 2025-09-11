package com.guideon.plan.parser;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@Getter
public class PdfPlanExtractor {

    // ===== 영업현황/생산/가동 =====
    private Double  salesAmountMn;     // 매출액 (백만원)
    private Double  exportAmountMn;    // 수출실적 (백만원)
    private Integer manuPercent;       // 자사제조 %
    private Integer outsourcePercent;  // 외주가공 %
    private Integer orderPercent;      // 주문판매비중 %
    private Integer directPercent;     // 직접판매비중 %
    private Integer workDaysPerMonth;  // 월평균 영업일
    private Integer hoursPerDay;       // 1일 평균 시간

    // ===== 자금소요 =====
    private Double fundFacilityMn;     // 시설(백만원)
    private Double fundWorkingMn;      // 운전(백만원)
    private Double fundTotalMn;        // 자금소요 합계(백만원)

    // ===== 자금조달 =====
    private Double planLoanMn;         // 본건 차입금
    private Double planSelfMn;         // 자체자금
    private Double planBankEtcMn;      // 은행차입금등(+기타까지 합산)
    private Double planTotalMn;        // 자금조달 합계

    // ===== 매출처 집중도 =====
    private Integer topClientRatio;    // 매출처 상위 거래처 비중(최댓값)

    /** PDF에서 텍스트를 읽어 파싱 */
    public static PdfPlanExtractor from(File pdf) throws Exception {
        try (PDDocument doc = PDDocument.load(pdf)) {
            PDFTextStripper ts = new PDFTextStripper();
            ts.setSortByPosition(true);
            ts.setSuppressDuplicateOverlappingText(true);
            ts.setStartPage(1);
            ts.setEndPage(doc.getNumberOfPages());   // 전체 페이지 명시
            String raw = ts.getText(doc);
            log.debug("PDF text (len={}):\n{}", raw.length(), raw);
            return parse(raw);
        }
    }

    /** 이미 추출한 텍스트(raw)를 파싱 */
    public static PdfPlanExtractor parse(String raw) {
        PdfPlanExtractor fx = new PdfPlanExtractor();
        String text = normalize(raw);

        // ===== 1) 영업현황 =====
        fx.salesAmountMn  = findD(text, "매\\s*출\\s*액\\s*([0-9.,]+)\\s*백\\s*만\\s*원|매출액\\s*([0-9.,]+)\\s*백만원");
        fx.exportAmountMn = findD(text, "수\\s*출\\s*실\\s*적(?:\\s*\\(해당시\\))?\\s*([0-9.,]+)\\s*백\\s*만\\s*원|수출실적\\s*([0-9.,]+)\\s*백만원");

        fx.manuPercent      = findI(text, "자\\s*사\\s*제\\s*조\\s*[:：]?\\s*([0-9]{1,3})\\s*%");
        fx.outsourcePercent = findI(text, "외\\s*주\\s*가\\s*공\\s*[:：]?\\s*([0-9]{1,3})\\s*%");
        fx.orderPercent     = findI(text, "주\\s*문\\s*판\\s*매\\s*비\\s*중\\s*[:：]?\\s*([0-9]{1,3})\\s*%|주문판매비중\\s*[:：]?\\s*([0-9]{1,3})\\s*%");
        fx.directPercent    = findI(text, "직\\s*접\\s*판\\s*매\\s*비\\s*중\\s*[:：]?\\s*([0-9]{1,3})\\s*%|직접판매비중\\s*[:：]?\\s*([0-9]{1,3})\\s*%");

        fx.workDaysPerMonth = findI(text, "월\\s*평\\s*균\\s*[:：]?\\s*([0-9]{1,2})\\s*일");
        fx.hoursPerDay      = findI(text, "1\\s*일\\s*평\\s*균\\s*[:：]?\\s*([0-9]{1,2})\\s*시\\s*간");

        // ===== 2) 자금소요 =====
        String needBlock = sliceBetween(text, "자\\s*금\\s*소\\s*요", "자\\s*금\\s*조\\s*달|$");
        if (needBlock == null) needBlock = text;

        // 패턴: "시설 운전 ... 내역 35 15 합계 50" (개행/공백 허용)
        Double[] needNums = matchThreeNumbers(
                needBlock,
                "시\\s*설\\s*운\\s*전[\\s\\S]{0,150}?내\\s*역\\s*([0-9.,]+)\\s+([0-9.,]+)[^0-9]+합\\s*계\\s*([0-9.,]+)"
        );
        if (needNums == null) {
            // Fallback: "내역 35 15 ... 합계 50"만 보이도록
            needNums = matchThreeNumbers(needBlock, "내\\s*역\\s*([0-9.,]+)\\s+([0-9.,]+)[^0-9]+합\\s*계\\s*([0-9.,]+)");
        }
        if (needNums != null) {
            fx.fundFacilityMn = needNums[0];
            fx.fundWorkingMn  = needNums[1];
            fx.fundTotalMn    = needNums[2];
        } else {
            // 라벨/숫자 줄 분리 Fallback
            fx.fundFacilityMn = orD(fx.fundFacilityMn, findLabelThenNumber(needBlock, "시\\s*설", 5));
            fx.fundWorkingMn  = orD(fx.fundWorkingMn,  findLabelThenNumber(needBlock, "운\\s*전", 5));
            fx.fundTotalMn    = orD(fx.fundTotalMn,    findLabelThenNumber(needBlock, "합\\s*계", 3));
        }

        // ===== 3) 자금조달 =====
        // 문서 끝에 "(백만원) 30 10 10 50" 같은 라인이 붙어도 잡히도록, 문서 전체에서
        // '(백만원)' 다음에 이어지는 숫자열의 "마지막 매치"를 사용.
        List<Double> lastAmounts = findLastAmountsAfterBaekManWon(text);
        if (!lastAmounts.isEmpty()) {
            // 숫자 4개 → [loan, self, bankEtc(+other), total]
            // 숫자 5개 → [loan, self, bank, other, total] → bankEtc에 (bank+other) 합산
            fx.planLoanMn    = lastAmounts.get(0);
            fx.planSelfMn    = lastAmounts.size() > 1 ? lastAmounts.get(1) : null;
            if (lastAmounts.size() >= 4) {
                double bank = lastAmounts.get(2) != null ? lastAmounts.get(2) : 0d;
                double other = (lastAmounts.size() == 5 && lastAmounts.get(3) != null) ? lastAmounts.get(3) : 0d;
                fx.planBankEtcMn = bank + other;
                fx.planTotalMn   = lastAmounts.get(lastAmounts.size()-1);
            }
        } else {
            // 블록 내부에서 헤더→다음줄 탐색 및 라벨→숫자 Fallback
            String planBlock = sliceBetween(text,
                    "자\\s*금\\s*조\\s*달",
                    "향\\s*후|주\\s*요\\s*거\\s*래\\s*처|생\\s*산\\s*품\\s*목|데\\s*이\\s*터|자\\s*금\\s*소\\s*요|$");
            if (planBlock == null) planBlock = sliceFrom(text, "자\\s*금\\s*조\\s*달");
            if (planBlock == null) planBlock = text;

            int headEnd = -1;
            Pattern header = Pattern.compile(
                    "본\\s*건\\s*(?:차\\s*입|대\\s*출)\\s*금.*?자\\s*(?:체|기)\\s*자\\s*금.*?은\\s*행\\s*차\\s*입\\s*금\\s*등.*?(기\\s*타)?\\s*.*?합\\s*계",
                    Pattern.CASE_INSENSITIVE | Pattern.DOTALL
            );
            Matcher hm = header.matcher(planBlock);
            if (hm.find()) headEnd = hm.end();

            List<Double> rowNums = new ArrayList<>();
            if (headEnd > 0) {
                String tail = planBlock.substring(headEnd);
                String[] lines = tail.split("\\R");
                Pattern numTok = Pattern.compile("([0-9]{1,3}(?:[ ,][0-9]{3})*|[0-9]+)(?:\\.[0-9]+)?");
                for (int i = 0; i < Math.min(lines.length, 6); i++) {
                    String ln = lines[i].trim();
                    if (ln.isEmpty()) continue;
                    Matcher nm = numTok.matcher(ln);
                    List<Double> tmp = new ArrayList<>();
                    while (nm.find()) tmp.add(parseD(nm.group(1)));
                    if (tmp.size() >= 3) { rowNums = tmp; break; }   // 한글 포함 라인도 허용
                }
            }
            if (!rowNums.isEmpty()) {
                fx.planLoanMn    = rowNums.get(0);
                fx.planSelfMn    = rowNums.size() > 1 ? rowNums.get(1) : null;
                if (rowNums.size() >= 4) {
                    double bank  = rowNums.get(2) != null ? rowNums.get(2) : 0d;
                    double other = (rowNums.size() >= 5 && rowNums.get(3) != null) ? rowNums.get(3) : 0d;
                    fx.planBankEtcMn = bank + other;
                    fx.planTotalMn   = rowNums.get(rowNums.size()-1);
                }
            } else {
                // 최후 라벨 매칭
                fx.planLoanMn    = findLabelThenNumber(planBlock, "본\\s*건\\s*(?:차\\s*입|대\\s*출)\\s*금", 3);
                fx.planSelfMn    = findLabelThenNumber(planBlock, "자\\s*(?:체|기)\\s*자\\s*금", 3);
                // '은행차입금등 기타'를 통합 라벨로도, 분리 라벨로도 시도
                Double bankEtc   = findLabelThenNumber(planBlock, "은\\s*행\\s*차\\s*입\\s*금\\s*등\\s*기\\s*타", 3);
                if (bankEtc == null) {
                    Double bank = findLabelThenNumber(planBlock, "은\\s*행\\s*차\\s*입\\s*금\\s*등", 3);
                    Double other= findLabelThenNumber(planBlock, "기\\s*타", 3);
                    bankEtc = addNullable(bank, other);
                }
                fx.planBankEtcMn = bankEtc;
                fx.planTotalMn   = findLabelThenNumber(planBlock, "합\\s*계", 3);
            }
        }

        // ===== 4) 매출처 상위비중 =====
        String salesBlock = sliceBetween(text, "매\\s*출\\s*처", "매\\s*입\\s*처|경\\s*영\\s*진|향\\s*후|$");
        List<Integer> pcts = findAllPercents(salesBlock);
        fx.topClientRatio = pcts.stream().max(Integer::compareTo).orElse(null);

        return fx;
    }

    /* ---------------------- 유틸 ---------------------- */

    private static String normalize(String s) {
        if (s == null) return "";
        String t = s;
        t = t.replace('\u00A0',' ')
                .replace("\u200B","").replace("\u200C","").replace("\u200D","");
        t = t.replace("Ÿ"," ").replace("※"," ");
        t = t.replaceAll("([0-9])\\s+%","$1%")
                .replaceAll("합\\s*계","합계")
                .replaceAll("백\\s*만\\s*원","백만원");
        t = t.replaceAll("\\s*[:：]\\s*"," : ");
        t = t.replaceAll("[ \\t]{2,}"," ");
        return t;
    }

    private static String sliceBetween(String text, String startRegex, String endRegex) {
        Pattern p = Pattern.compile(startRegex + "[\\s\\S]*?" + endRegex, Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(text == null ? "" : text);
        if (!m.find()) return null;
        String block = m.group();
        Matcher me = Pattern.compile(endRegex, Pattern.CASE_INSENSITIVE).matcher(block);
        if (me.find()) return block.substring(0, me.start());
        return block;
    }
    private static String sliceFrom(String text, String startRegex) {
        Matcher m = Pattern.compile(startRegex + "[\\s\\S]*", Pattern.CASE_INSENSITIVE)
                .matcher(text == null ? "" : text);
        return m.find() ? m.group() : null;
    }

    /** "(백만원) 30 10 10 50" 같은 라인의 '마지막 매치' 숫자 배열 반환 */
    private static List<Double> findLastAmountsAfterBaekManWon(String text) {
        List<Double> last = new ArrayList<>();
        Pattern amountLine = Pattern.compile(
                "\\(\\s*백\\s*만\\s*원\\s*\\)\\s*((?:\\d[\\d.,]*\\s+){2,4}\\d[\\d.,]*)",
                Pattern.CASE_INSENSITIVE | Pattern.DOTALL
        );
        Matcher am = amountLine.matcher(text == null ? "" : text);
        while (am.find()) {
            String seq = am.group(1);
            List<Double> nums = new ArrayList<>();
            Matcher nm = Pattern.compile("\\d[\\d.,]*").matcher(seq);
            while (nm.find()) nums.add(parseD(nm.group()));
            if (!nums.isEmpty()) last = nums; // 마지막 매치 유지
        }
        return last;
    }

    private static Double[] matchThreeNumbers(String block, String regex) {
        if (block == null) return null;
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.DOTALL).matcher(block);
        if (m.find()) {
            Double a = parseD(m.group(1));
            Double b = parseD(m.group(2));
            Double c = parseD(m.group(3));
            if (a != null && b != null && c != null) return new Double[]{a,b,c};
        }
        return null;
    }

    /** 라벨 줄에서 같은 줄 또는 다음 N줄에서 숫자 1개 */
    private static Double findLabelThenNumber(String block, String labelRegex, int lookaheadLines) {
        if (block == null) return null;
        String[] lines = block.split("\\R");
        Pattern lab = Pattern.compile(labelRegex, Pattern.CASE_INSENSITIVE);
        Pattern num = Pattern.compile("([0-9]{1,3}(?:[ ,][0-9]{3})*|[0-9]+)(?:\\.[0-9]+)?");
        for (int i=0;i<lines.length;i++) {
            if (!lab.matcher(lines[i]).find()) continue;
            for (int k=0; k<=lookaheadLines && i+k<lines.length; k++) {
                String ln = lines[i+k];
                Matcher nm = num.matcher(ln);
                if (nm.find()) return parseD(nm.group(1));
            }
        }
        return null;
    }

    private static List<Integer> findAllPercents(String block) {
        List<Integer> out = new ArrayList<>();
        Matcher m = Pattern.compile("\\b([1-9]?[0-9]|100)\\s*%", Pattern.MULTILINE)
                .matcher(block == null ? "" : block);
        while (m.find()) {
            Integer v = parseI(m.group(1));
            if (v != null) out.add(v);
        }
        return out;
    }

    private static Double findD(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
                .matcher(text == null ? "" : text);
        while (m.find()) {
            for (int g=1; g<=m.groupCount(); g++) {
                String gStr = m.group(g);
                if (gStr != null) return parseD(gStr);
            }
        }
        return null;
    }
    private static Integer findI(String text, String regex) {
        Matcher m = Pattern.compile(regex, Pattern.CASE_INSENSITIVE | Pattern.MULTILINE | Pattern.DOTALL)
                .matcher(text == null ? "" : text);
        while (m.find()) {
            for (int g=1; g<=m.groupCount(); g++) {
                String gStr = m.group(g);
                if (gStr != null) return parseI(gStr);
            }
        }
        return null;
    }

    private static Double parseD(String s){
        try { return Double.valueOf(s.replaceAll("[^0-9.]", "")); }
        catch(Exception e){ return null; }
    }
    private static Integer parseI(String s){
        try { return Integer.valueOf(s.replaceAll("[^0-9]", "")); }
        catch(Exception e){ return null; }
    }

    private static Double orD(Double a, Double b){ return a != null ? a : b; }
    private static Double addNullable(Double a, Double b){
        if (a == null && b == null) return null;
        double x = a == null ? 0d : a;
        double y = b == null ? 0d : b;
        return x + y;
    }
}
