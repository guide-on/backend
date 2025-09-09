package com.guideon.document.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MyDataSyncRequest {

    private List<Long> documentIds;
    private MyDataAgreements agreements;

    @Data
    public static class MyDataAgreements {
        private Boolean serviceTerms;
        private Boolean privacyPolicy;
        private Boolean thirdPartyConsent;

        public boolean isAllAgreed() {
            return Boolean.TRUE.equals(serviceTerms) &&
                    Boolean.TRUE.equals(privacyPolicy) &&
                    Boolean.TRUE.equals(thirdPartyConsent);
        }
    }
}
