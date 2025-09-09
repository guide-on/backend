package com.guideon.industry.catalog.infra;

import com.guideon.industry.catalog.domain.IndustryTag;

import java.util.List;
import java.util.Optional;

public interface IndustryTagCatalog {
    Optional<IndustryTag> find(String id);
    List<IndustryTag> findAll();
    List<String> codesOf(String id);
}
