package eu.researchalps.workflow.website;

import eu.researchalps.db.model.Link;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class WebsiteProvider implements FullStructureProvider<List<Website>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet(FullStructureField.STRUCTURE);

    @Autowired
    private WebsiteRepository repository;

    @Override
    public boolean canProvide(FullStructure structure) {
        return structure.getStructure().getLinks() != null && !structure.getStructure().getLinks().isEmpty();
    }

    @Override
    public List<Website> computeField(FullStructure structure) {
        List<String> websiteIds = structure.getStructure().getLinks().stream().map(Link::getId).filter(Objects::nonNull).collect(Collectors.toList());
        return ((List<Website>) repository.findAll(websiteIds)).stream().filter(it -> it.getPageCount() > 0).collect(Collectors.toList());
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.WEBSITES;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
