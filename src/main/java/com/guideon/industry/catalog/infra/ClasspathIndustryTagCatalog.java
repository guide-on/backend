package com.guideon.industry.catalog.infra;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.guideon.industry.catalog.domain.IndustryTag;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public class ClasspathIndustryTagCatalog implements IndustryTagCatalog {
    private final Map<String, IndustryTag> byId;
    public ClasspathIndustryTagCatalog(Resource jsonResource) {
        try (InputStream in = jsonResource.getInputStream()) {
            var om = new ObjectMapper();
            var list = om.readValue(in, new TypeReference<List<IndustryTag>>(){});
            list.forEach(t -> {
                if (t.getCodes()==null) t.setCodes(Collections.emptyList());
                t.setCodes(t.getCodes().stream()
                        .map(s -> s.replaceAll("\\s+",""))
                        .filter(s -> s.matches("^\\d{2}$")) // 2자리만 허용
                        .distinct()
                        .collect(Collectors.toList()));
            });
            this.byId = new HashMap<>();
            for (var t : list) this.byId.put(t.getId(), t);
        } catch (Exception e) {
            throw new IllegalStateException("industry-tags 파일 로드 실패", e);
        }
    }
    public Optional<IndustryTag> find(String id){ return Optional.ofNullable(byId.get(id)); }
    public List<IndustryTag> findAll(){ return new ArrayList<>(byId.values()); }
    public List<String> codesOf(String id){ return find(id).map(IndustryTag::getCodes).orElseGet(List::of); }
}
