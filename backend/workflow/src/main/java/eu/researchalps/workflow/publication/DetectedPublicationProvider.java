package eu.researchalps.workflow.publication;

import eu.researchalps.db.model.full.FSPublication;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.workflow.full.FullStructureProvider;
import com.google.common.collect.Sets;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class DetectedPublicationProvider implements FullStructureProvider<List<FSPublication>> {
    public static final Set<FullStructureField> FIELDS = Sets.newHashSet(FullStructureField.WEBSITES, FullStructureField.PUBLICATIONS);

    @Autowired
    private PublicationRepository publicationRepository;

    @Override
    public List<FSPublication> computeField(FullStructure structure) {

        // Add extracted publications
        if (structure.getWebsites() != null) {
            Set<String> alreadyPresent = new HashSet<>();
            if (structure.getPublications() != null) {
                structure.getPublications().stream().map(FSPublication::getId).forEach(alreadyPresent::add);
            }
            Set<String> extractedIds = structure.getWebsites().stream()
                    .map(website -> Stream.concat(
                            website.getExtractedPublications().stream(),
                            website.getResolvedPublications().stream()
                    ))
                    .flatMap(Function.identity())
                    .filter(it -> !alreadyPresent.contains(it))
                    .collect(Collectors.toSet());
            Iterable<Publication> publications = publicationRepository.findAll(extractedIds);
            // Transform project to FSProject and associate light structures to projects
            return StreamSupport.stream(publications.spliterator(), false)
                    .map(p -> PublicationProvider.toFSPublication(p, true))
                    .collect(Collectors.toList());
        }
        return null;
    }

    @Override
    public FullStructureField getField() {
        return FullStructureField.DETECTED_PUBLICATIONS;
    }

    @Override
    public Set<FullStructureField> getDependencies() {
        return FIELDS;
    }
}
