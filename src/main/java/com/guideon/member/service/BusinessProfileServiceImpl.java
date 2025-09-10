package com.guideon.member.service;

import com.guideon.member.domain.BusinessProfileVO;
import com.guideon.member.dto.BusinessProfileDTO;
import com.guideon.member.mapper.BusinessProfileMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.NoSuchElementException;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class BusinessProfileServiceImpl implements BusinessProfileService {

    private final BusinessProfileMapper mapper;

    @Override
    public boolean existsBizRegNo(String rawBizRegNo) {
        if (rawBizRegNo == null) throw new IllegalArgumentException("사업자번호는 필수입니다.");
        String norm = rawBizRegNo.replaceAll("[^0-9]", "");
        if (!norm.matches("^\\d{10}$")) {
            throw new IllegalArgumentException("유효하지 않은 사업자번호 형식입니다.");
        }
        return mapper.existsBizRegNo(norm) > 0;
    }

    @Override
    public BusinessProfileDTO getBusinessProfile(Long memberId) {
        BusinessProfileVO business = Optional.ofNullable(mapper.getBusinessProfile(memberId))
                .orElseThrow(NoSuchElementException::new);
        return BusinessProfileDTO.of(business);
    }
}
