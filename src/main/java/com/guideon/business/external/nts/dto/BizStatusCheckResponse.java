package com.guideon.business.external.nts.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@ApiModel(description = "사업자 상태(계속/휴업/폐업/미등록) 확인 결과")
public class BizStatusCheckResponse {
    @ApiModelProperty(value = "true=계속사업자(01), false=그 외", example = "true")
    private final boolean active;

    @ApiModelProperty(value = "국세청 상태 코드 (01 계속, 02 휴업, 03 폐업)", example = "01")
    private final String code;

    @ApiModelProperty(value = "국세청 상태 라벨", example = "계속사업자")
    private final String label;

    @ApiModelProperty(value = "에러 코드(성공 시 null)")
    private final BizStatusError error;

    @ApiModelProperty(value = "사용자 노출 메시지(성공 시 null)", example = "국세청에 등록되지 않은 사업자등록번호입니다.")
    private final String message;

    @ApiModelProperty(value = "정규화된 사업자등록번호(숫자 10자리)", example = "1234567890")
    private final String bno;

    /* 편의 팩토리 */
    public static BizStatusCheckResponse ok(String bno, String code, String label) {
        return BizStatusCheckResponse.builder()
                .active(true).bno(bno).code(code).label(label)
                .build();
    }

    public static BizStatusCheckResponse invalidBno(String raw, String message) {
        return BizStatusCheckResponse.builder()
                .active(false).error(BizStatusError.INVALID_BNO)
                .message(message)
                .bno(raw)
                .build();
    }

    public static BizStatusCheckResponse notRegistered(String bno) {
        return BizStatusCheckResponse.builder()
                .active(false).error(BizStatusError.NOT_REGISTERED)
                .message("국세청에 등록되지 않은 사업자등록번호입니다.")
                .bno(bno)
                .build();
    }

    public static BizStatusCheckResponse inactive(String bno, String code, String label, String msg) {
        return BizStatusCheckResponse.builder()
                .active(false).bno(bno).code(code).label(label)
                .error(BizStatusError.INACTIVE).message(msg)
                .build();
    }

    public static BizStatusCheckResponse unknown(String bno, String code, String label) {
        return BizStatusCheckResponse.builder()
                .active(false).bno(bno).code(code).label(label)
                .error(BizStatusError.UNKNOWN_STATUS)
                .message("사업자 상태를 확인할 수 없습니다. 잠시 후 다시 시도해주세요.")
                .build();
    }
}
