package com.guideon.region.service;

import com.guideon.region.dto.SidoDTO;
import com.guideon.region.mapper.RegionMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RegionServiceImpl implements RegionService {
    private final RegionMapper regionMapper;

    // 필요하면 @Cacheable("sidoList")
    @Transactional(readOnly = true)
    public List<SidoDTO> getSidoList() {
        return regionMapper.selectSidoList();
    }
}
