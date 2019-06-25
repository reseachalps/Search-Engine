package eu.researchalps.workflow.publication;

import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.model.publication.PublicationAffiliation;
import eu.researchalps.db.model.publication.PublicationAuthor;
import eu.researchalps.db.model.publication.PublicationStructure;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.util.SimpleMerge;
import eu.researchalps.workflow.full.FullStructureService;
import com.google.common.collect.Sets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

import static org.springframework.util.StringUtils.isEmpty;

/**
 * Created by loic on 01/04/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class PublicationMergeService {

    private static final Logger log = LoggerFactory.getLogger(PublicationMergeService.class);

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private FullStructureService fullStructureService;

    public void mergeAndSave(Publication publication) {
        // Contains all FS Ids
        Set<String> toRefresh = Sets.newHashSet();
        if (mergeAndSave(publication, toRefresh) != null) {
            refreshFS(toRefresh);
        }
    }

    public String mergeAndSave(Publication publication, Set<String> toRefresh) {
        String publicationId = publication.computeId();
        if (publicationId == null) {
            log.warn("Not enough data to make a decent identifier (title and first author's last name), ignoring...");
            return null;
        }
        publication.setId(publicationId);
        List<Publication> similar = publicationRepository.findSimilar(publicationId, publication.getIdentifiers());
        similar.stream().map(Publication::getStructures).flatMap(Collection::stream).forEach(toRefresh::add);
        Set<Publication> obsoleteIds = similar.stream().filter(it -> !it.getId().equals(publicationId)).collect(Collectors.toSet());
        if (!obsoleteIds.isEmpty()) {
            // Remove entries that will be merged
            publicationRepository.delete(obsoleteIds);
        }
        if (similar.isEmpty()) {
            // Create a new one
            computeStructures(publication);
            publication = publicationRepository.save(publication);
        } else if (similar.size() == 1) {
            // Merge with one
            Publication targetToMerge = similar.get(0);
            mergePublication(targetToMerge, publication);
            computeStructures(targetToMerge);
            publication = publicationRepository.save(targetToMerge);
        } else {
            // Two publications are actually one (merged with a unique reference)
            Publication targetToMerge = similar.get(0);
            for (Publication p : similar) {
                mergePublication(p, targetToMerge);
            }
            mergePublication(targetToMerge, publication);
            computeStructures(targetToMerge);
            publication = publicationRepository.save(targetToMerge);
        }
        toRefresh.addAll(publication.getStructures());
        return publicationId;
    }

    protected void computeStructures(Publication publication) {
        Set<String> structures = new HashSet<>();

        // Compute structures usging publication authors
        List<String> structureCodes = publication.getAuthors().stream()
                .map(PublicationAuthor::getAffiliations)
                .filter(it -> it != null)
                .flatMap(Collection::stream)
                .map(PublicationAffiliation::getStructure)
                .filter(it -> it != null)
                .map(PublicationStructure::getCode)
                .filter(it -> it != null)
                .collect(Collectors.toList());
        structures.addAll(structureRepository.findIdsByInstitutionCode(structureCodes).stream().map(Structure::getId).collect(Collectors.toList()));

        // Add existing validated structures if needed
        if (publication.getValidatedStructures() != null) {
            // Make sure that ids are not null (old bug)
            publication.getValidatedStructures().stream().filter(it -> it != null).forEach(structures::add);
        }

        publication.setStructures(structures);
    }

    protected void mergePublication(Publication targetToMerge, Publication publication) {
        // If same reference
        if (publication == targetToMerge) return;
        // Merge
        SimpleMerge.mergeSimpleAttributes(targetToMerge, publication);
        // Ids
        SimpleMerge.mergeSimpleAttributes(targetToMerge.getIdentifiers(), publication.getIdentifiers());
        targetToMerge.getIdentifiers().addAll(publication.getIdentifiers());
        SimpleMerge.mergeSimpleAttributes(targetToMerge.getSource(), publication.getSource());
        publication.setAuthors(cleanAuthors(mergeAuthors(targetToMerge.getAuthors(), publication.getAuthors())));
        publication.setThesisDirectors(mergeAuthors(targetToMerge.getThesisDirectors(), publication.getThesisDirectors()));
        if (publication.getThematics() != null && !publication.getThematics().isEmpty()) {
            targetToMerge.setThematics(publication.getThematics());
        }
        // merge validated structures
        Set<String> validatedStructures = new HashSet<>();
        if (publication.getValidatedStructures() != null) validatedStructures = publication.getValidatedStructures();
        // Make sure that null is not part of the validated structures
        validatedStructures.remove(null);
        targetToMerge.setValidatedStructures(validatedStructures);

        if (publication.getSources() != null) {
            targetToMerge.setSources(publication.getSources());
        }
    }

    private List<PublicationAuthor> cleanAuthors(List<PublicationAuthor> authors) {
        if (authors == null) {
            return Collections.emptyList();
        }
        return authors.stream().filter(it -> trustScore(it) > 0).collect(Collectors.toList());
    }

    /**
     * Provide the merge of two authors. Actually, this does not merge at all, this selects the best authors list
     * using a trustScore (meaning how complete the data is).
     *
     * If trust level is identical then, the second list is selected to provide freshness of data.
     *
     * @param authors First list
     * @param authors2 Second list
     * @return The list to set
     */
    protected static List<PublicationAuthor> mergeAuthors(List<PublicationAuthor> authors, List<PublicationAuthor> authors2) {
        if (authors == null || authors.isEmpty()) {
            return authors2;
        }
        if (authors2 == null || authors2.isEmpty()) {
            return authors;
        }
        // Ok both are non null and non empty
        int maxTrust = authors.stream().map(PublicationMergeService::trustScore).max(Integer::compare).get();
        int maxTrust2 = authors2.stream().map(PublicationMergeService::trustScore).max(Integer::compare).get();
        if (maxTrust2 >= maxTrust) {
            return authors2;
        }
        return authors;
    }

    /**
     * Return a score of trustness of this author instance
     *
     * Side effect: cleans affiliation if it is useless
     *
     * @param author The author instance
     * @return 0 if empty
     */
    protected static int trustScore(PublicationAuthor author) {
        int result = 0;
        if (!isEmpty(author.getFirstName())) {
            result += 1;
        }
        if (!isEmpty(author.getLastName())) {
            result += 1;
        }
        boolean hasInterestingAffiliation = false;
        List<PublicationAffiliation> affiliations = author.getAffiliations();
        if (affiliations != null && affiliations.size() > 0) {
            PublicationAffiliation affiliation = affiliations.get(0);
            PublicationStructure structure = affiliation.getStructure();
            if (structure != null) {
                if (!isEmpty(structure.getId()) || !isEmpty(structure.getLabel()) || !isEmpty(structure.getCode())) {
                    hasInterestingAffiliation = true;
                    result += 1;
                }
            }
        }
        // Clean useless affiliation
        if (!hasInterestingAffiliation) {
            author.setAffiliations(null);
        }
        return result;
    }

    public void refreshFS(Collection<String> structures) {
        for (String structure : structures) {
            fullStructureService.delayedRefresh(structure, FullStructureField.PUBLICATIONS);
        }
    }
}
