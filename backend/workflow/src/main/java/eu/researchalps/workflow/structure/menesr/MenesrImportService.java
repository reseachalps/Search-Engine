package eu.researchalps.workflow.structure.menesr;
/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.*;
import eu.researchalps.db.model.*;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.ProjectRepository;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.full.FullStructureTransaction;
import eu.researchalps.workflow.publication.PublicationMergeService;
import eu.researchalps.workflow.website.WebsiteAnalysisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Import service
 */
@Service
public class MenesrImportService {
    private static final Logger log = LoggerFactory.getLogger(MenesrImportService.class);

    private RepositoryLock<Structure, String, StructureRepository> structureRepository;

    private RepositoryLock<Project, String, ProjectRepository> projectRepository;

    private RepositoryLock<Publication, String, PublicationRepository> publicationRepository;

    @Autowired
    private PublicationMergeService publicationMergeService;

    @Autowired
    private FullStructureService fsservice;

    @Autowired
    private WebsiteAnalysisService analysisService;

    @Autowired
    private MenesrFetcher fetcher;

    public int fetchStructures() {
        List<Structure> structures = fetcher.fetchPublicStructures();
        structures.addAll(fetcher.fetchCompanies());

        Set<String> idsToUpdate = structures.stream().map(Structure::getId).collect(Collectors.toSet());

        Set<String> idsToDelete = new HashSet<>();

        Map<String, Structure> idx = structures.stream().collect(Collectors.toMap(Structure::getId, it -> it));
        for (Structure structure : structures) {
            if (structure.getChildren() == null)
                continue;
            for (ParentReference child : structure.getChildren()) {
                if (child.getId() == null || child.getId().isEmpty())
                    continue;
                Structure childStructure = idx.get(child.getId());
                if (childStructure.getParent() == null)
                    childStructure.setParent(new LinkedList<>());
                childStructure.getParent().add(new ParentReference(structure.getId(), child.isExclusive()));
            }
            structure.setChildren(null);
        }

        Set<Link> linksToAnalyze = structures.stream().map(Structure::getLinks).filter(Objects::nonNull).flatMap(Collection::stream).filter(it -> it.getId() != null).collect(Collectors.toSet());

        structureRepository.updateGlobally(structureRepository -> {
            // Deleted ids are ids that are present before but not anymore
            structureRepository.streamAllIds().forEach(idsToDelete::add);
            idsToDelete.removeAll(idsToUpdate);

            // Save all structures in the repository
            // Delete is here to prevent phantom entries
            structureRepository.deleteByIds(idsToDelete);
            structureRepository.save(structures);
        });

        for (String id : idsToUpdate) {
            try (FullStructureTransaction tx = fsservice.tx(id, true)) {
                tx.refresh(FullStructureField.STRUCTURE);
                tx.refresh(FullStructureField.CHILDREN);
                tx.refresh(FullStructureField.PARENTS);
                tx.save(false, false);
            }
        }

        for (String id : idsToDelete) {
            // will trigger the deletion as structure is not present anymore
            fsservice.refresh(id, FullStructureField.STRUCTURE);
        }

        for (Link link : linksToAnalyze) {
            CrawlMode mode = link.getMode();
            if (mode == null) {
                mode = inferCrawlMode(link.getId());
            }
            analysisService.analyze(link.getUrl(), mode, false);
        }

        return idsToUpdate.size();
    }

    public static CrawlMode inferCrawlMode(String link) {
        if (link.contains("?")) {
            return CrawlMode.SINGLE_PAGE;
        }
        if (!link.contains("/")) {
            return CrawlMode.FULL_DOMAIN;
        }
        String page = link.substring(link.lastIndexOf('/'));
        if (page.contains(".")) {
            return CrawlMode.SINGLE_PAGE;
        }
        return CrawlMode.SUBPATH;
    }

    public int fetchProjects() {
        List<Project> projects = fetcher.fetchProjects();
        Set<String> allProjectIds = projects.stream().map(Project::getId).collect(Collectors.toSet());

        Set<String> impactedCompanies = projectRepository.updateGloballyAndReturn(projectRepository -> {
            // Get all impacted company ids
            projectRepository.deleteAll();
            Set<String> companyIds = Stream.concat(projects.stream(), projectRepository.findStructuresByProjectIds(allProjectIds).stream())
                    .map(Project::getStructures)
                    .filter(it -> it != null)
                    .flatMap(Collection::stream)
                    .filter(ps -> !ps.isExternal())
                    .map(ProjectStructure::getId)
                    .collect(Collectors.toSet());
            projectRepository.save(projects);
            return companyIds;
        });

        // Ignore unknown structures
        List<Structure> byIdsLight = structureRepository.readOnly().findByIdsLight(impactedCompanies);

        for (Structure structure : byIdsLight) {
            fsservice.refresh(structure.getId(), FullStructureField.PROJECTS);
        }

        return allProjectIds.size();
    }


    public long fetchPublications() {
        // empty all publications
        // #yolo
        publicationRepository.updateGlobally(CrudRepository::deleteAll);

        // insert all the new publications
        long publications = fetcher.fetchPublications(publication -> {
            try {
                publicationMergeService.mergeAndSave(publication, new HashSet<>());
            } catch (Exception e) {
                log.error("Cannot merge and save publication " + publication.getId() + " reason: " + e.getMessage());
            }
        });

        // import the new repositories
        fsservice.getRepository().selectAllIds().peek(id -> fsservice.refresh(id, FullStructureField.PUBLICATIONS)).count();

        return publications;
    }

    @Autowired
    public void setProjectRepository(ProjectRepository projectRepository) {
        this.projectRepository = RepositoryLock.get(projectRepository);
    }

    @Autowired
    public void setStructureRepository(StructureRepository structureRepository) {
        this.structureRepository = RepositoryLock.get(structureRepository);
    }


    @Autowired
    public void setPublicationRepository(PublicationRepository publicationRepository) {
        this.publicationRepository = RepositoryLock.get(publicationRepository);
    }
}
