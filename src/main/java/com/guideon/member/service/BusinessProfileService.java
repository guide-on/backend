package com.guideon.member.service;

import com.guideon.member.dto.BusinessProfileDTO;

public interface BusinessProfileService {
    boolean existsBizRegNo(String rawBizRegNo);
    BusinessProfileDTO getBusinessProfile(Long memberId);
}
