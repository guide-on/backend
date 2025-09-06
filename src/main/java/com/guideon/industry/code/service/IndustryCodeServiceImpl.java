package com.guideon.industry.code.service;

import com.guideon.common.pagination.Page;
import com.guideon.common.pagination.PageRequest;
import com.guideon.industry.code.dto.Ksic5DTO;
import com.guideon.industry.code.mapper.IndustryCodeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class IndustryCodeServiceImpl implements IndustryCodeService {

    private final IndustryCodeMapper mapper;

    @Override
    public Page<Ksic5DTO> getKsic5CodesPage(PageRequest req, String code, String name) {
        // 방어적 정규화: code는 숫자만, 최대 5자리(prefix 검색)
        String codeNorm = null;
        if (code != null) {
            codeNorm = code.replaceAll("\\s+", "").replaceAll("[^0-9]", "");
            if (codeNorm.length() > 5) codeNorm = codeNorm.substring(0, 5);
            if (codeNorm.isEmpty()) codeNorm = null;
        }
        String nameNorm = (name != null) ? name.trim() : null;
        if (nameNorm != null && nameNorm.isEmpty()) nameNorm = null;

        List<Ksic5DTO> rows = mapper.selectKsic5Paged(req.getOffset(), req.getAmount(), codeNorm, nameNorm);
        int total = mapper.countKsic5(codeNorm, nameNorm);

        return Page.of(req, total, rows);
    }
}