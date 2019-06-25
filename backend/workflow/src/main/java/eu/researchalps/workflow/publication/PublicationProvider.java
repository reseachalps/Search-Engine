package eu.researchalps.workflow.publication;

import eu.researchalps.db.model.full.FSPublication;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class PublicationProvider implements FullStructureProvider<List<FSPublication>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet();

    @Autowired
    private PublicationRepository publicationRepository;


    @Autowired
    private StructureRepository structureRepository;

    @Override
    public List<FSPublication> computeField(FullStructure structure) {
        return publicationRepository
                .findByStructures(structure.getId())
                .stream().map(p -> toFSPublication(p, false))
                .collect(Collectors.toList());
    }

    public static FSPublication toFSPublication(Publication p, boolean webdetected) {
        FSPublication fsPublication = new FSPublication(p);
        fsPublication.setWebDetected(webdetected);
        return fsPublication;
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.PUBLICATIONS;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
