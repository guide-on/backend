package com.guideon.ocr.util;

import com.guideon.ocr.dto.BizRegOcrResultDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class BizRegParser {
    // 등록번호 / 사업자등록번호
    private static final Pattern P_BIZNO = Pattern.compile(
            "(사업자\\s*등록\\s*번호|등록번호)\\s*[:：]?\\s*([0-9]{3}[-\\s]?[0-9]{2}[-\\s]?[0-9]{5})"
    );

    // 개업(연월일)
//    private static final Pattern P_DATE = Pattern.compile(
//            "(개업\\s*연월일|개업\\s*일자|개업\\s*일)\\s*[:：]?\\s*([0-9]{4}[\\.\\-/년]\\s*[0-9]{1,2}[\\.\\-/월]\\s*[0-9]{1,2})"
//    );

    private static final Pattern P_DATE = Pattern.compile(
            "(개업.?일|개업.?연월일|개.?업.?일.?자)?\\s*([0-9]{4}[.\\-/년]\\s*[0-9]{1,2}[.\\-/월]\\s*[0-9]{1,2})");

    // 성명/대표자 (느슨)
    private static final Pattern P_OWNER_LOOSE = Pattern.compile(
            "(?:성\\s*명|대표자|대표)\\s*[:：]?\\s*([\\p{IsHangul}a-zA-Z\\s]{2,30}?)\\s*" +
                    "(?=생년월일|등록번호|사업장\\s*소재지|사업의\\s*종류|개업\\s*연월일|국세청|$)"
    );

    // 성명이 '명:'만 남는 경우(성 누락) 대응
    private static final Pattern P_OWNER_FALLBACK = Pattern.compile(
            "(?:^|\\s)명\\s*[:：]\\s*([\\p{IsHangul}a-zA-Z\\s]{2,30}?)\\s*" +
                    "(?=생년월일|등록번호|사업장\\s*소재지|사업의\\s*종류|개업\\s*연월일|국세청|$)"
    );

    // 상호/법인명 (라벨 깨짐 허용: '상 성 개 ... 호')
    private static final Pattern P_COMPANY_LOOSE = Pattern.compile(
            "(?:상\\s*.{0,4}?\\s*호|상호|상호명|법인명\\(상호\\))\\s*[:：]?\\s*"
                    + "([\\p{IsHangul}a-zA-Z0-9\\s\\(\\)\\.&]+?)"
                    + "(?=\\s+(?:성\\s*명|명\\s*:|대표자|대표|사업장\\s*소재지|사업장소재지|사업의\\s*종류|등록번호|개업\\s*연월일|생년월일|국세청|$))"
    );

    // 상호/성명을 한 블록에서 동시에 잡기 (…호: <회사> … 명: <대표자>)
    private static final Pattern P_COMPANY_OWNER_BLOCK = Pattern.compile(
            "(?:상\\S{0,6}?호|상호|상호명)\\s*[:：]?\\s*([\\p{IsHangul}a-zA-Z0-9\\s\\(\\)\\.&]+?)\\s+"
                    + "(?:성\\s*)?명\\s*[:：]\\s*([\\p{IsHangul}a-zA-Z\\s]{2,30})"
    );

    // 업태/종목
    private static final Pattern P_BIZTYPE_ANYLINE = Pattern.compile(
            "업태\\s*[:：]?\\s*([^\\r\\n，,]+?)\\s*(?=\\R|\\s+종목|$)"
    );
    private static final Pattern P_BIZITEM_ANYLINE = Pattern.compile(
            "종목\\s*[:：]?\\s*([^\\r\\n]+?)\\s*(?=\\R|\\s+(?:사업장\\s*소재지|주소|등록번호|개업|국세청|$))"
    );
    private static final Pattern P_KIND_HEADER     = Pattern.compile("사업의\\s*종류");

    // 주소 키워드
    private static final List<String> ADDRESS_KEYS = List.of(
            "사업장 소재지", "사업장소재지", "사업장 주소", "소재지", "주소"
    );

    /* ========= API ========= */

    public static BizRegOcrResultDTO parse(String text) {
        String norm = normalizeKeepLines(text);   // 줄 유지(주소/블록 탐색)
        String flat = flatten(norm);              // 줄 제거(라벨 깨짐 대응)
        List<String> lines = splitLines(norm);

        // 1) 등록번호
        String bizNo = findGroup(P_BIZNO, norm, 2);
        if (bizNo == null) bizNo = fallbackBizNo(norm);
        if (bizNo != null) bizNo = bizNo.replaceAll("[^0-9]", "");

        // 2) 개업일
        String opened = findGroup(P_DATE, norm, 2);
        if (opened != null) opened = normalizeDate(opened);

        // 3) 상호/성명
        String company = findGroup(P_COMPANY_LOOSE, flat, 1);
        String owner   = findGroup(P_OWNER_LOOSE,   flat, 1);

        if (isEmpty(owner)) {
            String t = findGroup(P_OWNER_FALLBACK, flat, 1);
            if (!isEmpty(t)) owner = t;
        }
        if (isEmpty(company) || isEmpty(owner)) {
            Matcher m = P_COMPANY_OWNER_BLOCK.matcher(flat);
            if (m.find()) {
                if (isEmpty(company)) company = m.group(1).trim();
                if (isEmpty(owner))   owner   = m.group(2).trim();
            }
        }

        // 4) 주소
        String addr = findAddress(lines);
        if (addr == null) addr = stitchAddressAfterKey(lines);

        // 5) 업태/종목
        String bizType  = firstGroup(P_BIZTYPE_ANYLINE, norm);
        String bizItems = firstGroup(P_BIZITEM_ANYLINE, norm);
        if (isEmpty(bizType) || isEmpty(bizItems)) {
            String[] pair = fromKindBlock(lines);
            if (isEmpty(bizType))  bizType  = pair[0];
            if (isEmpty(bizItems)) bizItems = pair[1];
        }

        owner   = stripAfter(owner,   "생년월일", "등록번호", "사업장소재지", "사업장 소재지", "사업의 종류");
        bizType = stripAfter(bizType, "종목", "사업장소재지", "사업장 소재지", "주소", "국세청");
        bizItems= stripAfter(bizItems,"사업장소재지", "사업장 소재지", "주소", "국세청");

        return BizRegOcrResultDTO.builder()
                .bizRegNo(bizNo)
                .companyName(safe(company))
                .ownerName(safe(owner))
                .bizType(safe(trimComma(bizType)))
                .bizItems(safe(trimComma(bizItems)))
                .address(safe(addr))
                .openedOn(opened)
                .build();
    }

    /* ========= helpers ========= */

    private static String normalizeKeepLines(String t) {
        if (t == null) return "";
        return t.replace('\u00A0',' ')
                .replace('·',' ').replace('ㆍ',' ')
                .replace("：",":")
                .replaceAll("[\\t\\r]+"," ")
                .replaceAll(" +"," ")
                .trim();
    }

    private static String flatten(String s) {
        // 줄을 공백으로 묶어 '상\n호', '성\n명'도 인식
        return s.replaceAll("\\s*\\R+\\s*", " ");
    }

    private static List<String> splitLines(String s) {
        String[] arr = s.split("\\R");
        List<String> out = new ArrayList<>(arr.length);
        for (String a : arr) {
            String z = a.trim();
            if (!z.isEmpty()) out.add(z);
        }
        return out;
    }

    private static String findGroup(Pattern p, String s, int group) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(group).trim() : null;
    }

    private static String firstGroup(Pattern p, String s) {
        Matcher m = p.matcher(s);
        return m.find() ? m.group(1).trim() : null;
    }

    private static String fallbackBizNo(String s) {
        Matcher only = Pattern.compile("\\b[0-9]{3}[-\\s]?[0-9]{2}[-\\s]?[0-9]{5}\\b").matcher(s);
        return only.find() ? only.group() : null;
    }

    private static String findAddress(List<String> lines) {
        for (String line : lines) {
            for (String key : ADDRESS_KEYS) {
                if (line.contains(key)) {
                    String a = line.replace(key,"").replace(":","").trim();
                    if (!a.isEmpty()) return a;
                }
            }
        }
        return null;
    }

    private static String stitchAddressAfterKey(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            for (String key : ADDRESS_KEYS) {
                if (lines.get(i).contains(key)) {
                    String base = lines.get(i).replace(key,"").replace(":","").trim();
                    StringBuilder sb = new StringBuilder(base);
                    // 다음 1~2줄 주소 이어붙임(다른 필드 나오면 중단)
                    for (int k = i + 1; k < Math.min(i + 3, lines.size()); k++) {
                        String nxt = lines.get(k);
                        if (looksLikeAnotherField(nxt)) break;
                        if (!sb.isEmpty()) sb.append(" ");
                        sb.append(nxt.trim());
                    }
                    String addr = sb.toString().replaceAll("\\s{2,}"," ").trim();
                    if (!addr.isEmpty()) return addr;
                }
            }
        }
        return null;
    }

    private static boolean looksLikeAnotherField(String s) {
        return s.contains("업태") || s.contains("종목")
                || s.contains("상호") || s.contains("법인명")
                || s.contains("성명") || s.contains("대표")
                || s.contains("등록번호") || s.contains("사업자등록번호")
                || s.contains("개업") || s.contains("생년월일")
                || s.contains("발급사유") || s.contains("공동사업자");
    }

    private static String[] fromKindBlock(List<String> lines) {
        for (int i = 0; i < lines.size(); i++) {
            if (P_KIND_HEADER.matcher(lines.get(i)).find()) {
                StringBuilder win = new StringBuilder(lines.get(i));
                if (i + 1 < lines.size()) win.append(" ").append(lines.get(i + 1));
                if (i + 2 < lines.size()) win.append(" ").append(lines.get(i + 2));
                String w = win.toString();
                String t = firstGroup(P_BIZTYPE_ANYLINE, w);
                String g = firstGroup(P_BIZITEM_ANYLINE, w);
                return new String[] { t, g };
            }
        }
        return new String[] { null, null };
    }

    private static String normalizeDate(String in) {
        String t = in.replace("년",".").replace("월",".").replace("일",".")
                .replaceAll("\\s+","");
        Matcher m = Pattern.compile("(\\d{4})[\\.\\-/](\\d{1,2})[\\.\\-/](\\d{1,2})").matcher(t);
        if (m.find()) {
            int y = Integer.parseInt(m.group(1));
            int mo = Integer.parseInt(m.group(2));
            int d = Integer.parseInt(m.group(3));
            try { return LocalDate.of(y, mo, d).toString(); } catch (Exception ignored) {}
        }
        return null;
    }

    private static String stripAfter(String s, String... keys) {
        if (s == null) return null;
        for (String k : keys) {
            int i = s.indexOf(k);
            if (i >= 0) { s = s.substring(0, i).trim(); }
        }
        return s;
    }

    private static boolean isEmpty(String s) { return s == null || s.trim().isEmpty(); }
    private static String  safe(String s)    { return s == null ? "" : s; }
    private static String  trimComma(String s){ return s == null ? null : s.replaceAll("^[,\\s]+|[,\\s]+$", ""); }
}
