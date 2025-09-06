package com.guideon.business.external.nts.dto;

import lombok.Data;

@Data
public class BusinessStatusItem {
    private String b_no;                // 사업자번호
    private String b_stt;               // 상태 (예: 계속사업자)
    private String b_stt_cd;            // 상태코드 (예: 01=계속사업자)
    private String tax_type;            // 과세유형
    private String tax_type_cd;         // 과세유형코드
    private String end_dt;              // 폐업일자(YYYYMMDD) - 없으면 null/빈문자
    private String utcc_yn;             // 단위과세전환여부(Y/N)
    private String tax_type_change_dt;  // 과세유형 변경일자
    private String invoice_apply_dt;    // 전자세금계산서 적용일자
    private String rbf_tax_type;        // 직전 과세유형 (v1.1)
    private String rbf_tax_type_cd;     // 직전 과세유형 코드 (v1.1)
}
