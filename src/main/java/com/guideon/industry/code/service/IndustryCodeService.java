package com.guideon.industry.code.service;

import com.guideon.common.pagination.Page;
import com.guideon.common.pagination.PageRequest;
import com.guideon.industry.code.dto.Ksic5DTO;

public interface IndustryCodeService {
    Page<Ksic5DTO> getKsic5CodesPage(PageRequest pageRequest, String code, String name);
}
