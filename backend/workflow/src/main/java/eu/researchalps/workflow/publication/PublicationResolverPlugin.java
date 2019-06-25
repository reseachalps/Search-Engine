package eu.researchalps.workflow.publication;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.website.WebsiteAnalysisService;
import eu.researchalps.workflow.website.publication.dto.ExtractedPublicationsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Created by loic on 02/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class PublicationResolverPlugin extends QueueComponent implements QueueListener<PublicationResolverPlugin.DOIPublicationsDTO> {
    private static MessageQueue<DOIPublicationsDTO> OUT = MessageQueue.get("PUBLICATION_RESOLVER_OUT", DOIPublicationsDTO.class);
    private static MessageQueue<ExtractedPublicationsDTO> IN = MessageQueue.get("PUBLICATION_RESOLVER", ExtractedPublicationsDTO.class);

    @Autowired
    private FullStructureService fullStructureService;

    @Autowired
    private WebsiteAnalysisService websiteAnalysisService;

    @Autowired
    private PublicationMergeService publicationMergeService;

    private RepositoryLock<Website, String, WebsiteRepository> websiteRepository;

    @Autowired
    public void setWebsiteRepository(WebsiteRepository repository) {
        this.websiteRepository = RepositoryLock.get(repository);
    }

    public void execute(ExtractedPublicationsDTO dto) {
        if ((dto.dois != null && dto.dois.size() > 0) || (dto.references != null && dto.references.size() > 0)) {
            queueService.push(dto, IN, OUT);
        }
    }

    @Override
    public void receive(DOIPublicationsDTO dto) {
        List<String> publications = merge(dto.publications, dto.id);
        if (dto.url != null) {
            // website update, we give all extracted publications to the website
            String websiteId = dto.url;
            websiteRepository.update(websiteId, tx -> {
                Website w = tx.get();
                w.setResolvedPublications(publications);
                tx.saveDeferred();
            });

            // Refresh full structures for this website
            websiteAnalysisService.refreshFSForWebsite(websiteId, true);
        } else if (dto.id != null) {
            // No action, merge has been done in previous merge function
        } else {
            throw new IllegalStateException("No ID or URL provided");
        }
    }

    private List<String> merge(List<Publication> publications, String structureId) {
        Set<String> toRefresh = new HashSet<>();
        List<String> ids = publications.stream().map(it -> {
            if (structureId != null) {
                it.getValidatedStructures().add(structureId);
            }
            // This publication is NOT to be 100% trusted as data is very sparse
            return publicationMergeService.mergeAndSave(it, toRefresh);
        }).filter(it -> it != null).collect(Collectors.toList());
        publicationMergeService.refreshFS(toRefresh);
        return ids;
    }

    @Override
    public MessageQueue<DOIPublicationsDTO> getQueue() {
        return OUT;
    }

    public static class DOIPublicationsDTO {
        public String url;
        public String id;
        public List<Publication> publications;
    }
}
