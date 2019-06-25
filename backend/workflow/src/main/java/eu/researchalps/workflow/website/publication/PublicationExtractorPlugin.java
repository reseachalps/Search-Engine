package eu.researchalps.workflow.website.publication;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.Website;
import eu.researchalps.workflow.publication.PublicationResolverPlugin;
import eu.researchalps.workflow.website.WebsiteAnalysisService;
import eu.researchalps.workflow.website.publication.dto.ExtractedPublicationsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by loic on 02/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class PublicationExtractorPlugin extends QueueComponent implements QueueListener<ExtractedPublicationsDTO> {
    private static MessageQueue<ExtractedPublicationsDTO> OUT = MessageQueue.get("PUBLICATION_EXTRACTOR_OUT", ExtractedPublicationsDTO.class);
    private static MessageQueue<In> IN = MessageQueue.get("PUBLICATION_EXTRACTOR", In.class);

    @Autowired
    private WebsiteAnalysisService service;

    @Autowired
    private PublicationResolverPlugin publicationResolverPlugin;

    public void execute(Website w) {
        queueService.push(new In(w.getId()), IN, OUT);
    }

    @Override
    public void receive(ExtractedPublicationsDTO dto) {
        publicationResolverPlugin.execute(dto);
    }

    @Override
    public MessageQueue<ExtractedPublicationsDTO> getQueue() {
        return OUT;
    }

    public static class In {
        public String url;

        public In(String url) {
            this.url = url;
        }

        public In() {
        }
    }
}
