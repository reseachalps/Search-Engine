package eu.researchalps.workflow.oai;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobInput;
import eu.researchalps.db.model.Identifier;
import eu.researchalps.db.model.publication.Publication;
import eu.researchalps.db.repository.PublicationRepository;
import eu.researchalps.workflow.publication.PublicationMergeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by loic on 18/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class OAIEntryHarvest extends QueueComponent implements QueueListener<OAIEntry> {
    public static final MessageQueue<ScheduledJobInput> QUEUE_FETCH = MessageQueue.get("OAI_FETCHER", ScheduledJobInput.class);
    public static final MessageQueue<OAIEntry> QUEUE = MessageQueue.get("OAI_ENTRIES", OAIEntry.class);

    private static final Logger log = LoggerFactory.getLogger(OAIEntryHarvest.class);
    public static final String OAI_IDENTIFIER_TYPE = "OAI";

    @Autowired
    private PublicationRepository publicationRepository;

    @Autowired
    private PublicationMergeService mergeService;

    @Override
    public void receive(OAIEntry oaiEntry) {
        Publication publication = oaiEntry.content;
        if (oaiEntry.deleted) {
            Identifier identifier = new Identifier();
            identifier.setId(oaiEntry.id);
            identifier.setType(OAI_IDENTIFIER_TYPE);
            List<Publication> similar = publicationRepository.findSimilar(null, Collections.singleton(identifier));
            if (similar.isEmpty()) {
                // We deleted something we did not know of, no problem
                return;
            } else if(similar.size() == 1) {
                publication = similar.get(0);
                publicationRepository.delete(publication);
                mergeService.refreshFS(publication.getStructures());
                return;
            } else {
                throw new IllegalStateException("Two publications with the same ids are in the database?");
            }
        }
        publication.setLastSourceDate(oaiEntry.date);
        mergeService.mergeAndSave(publication);
    }

    public static class Request {
        public String url;
        public String meta_prefix;

        public Request(String url, String meta_prefix) {
            this.url = url;
            this.meta_prefix = meta_prefix;
        }

        public Request() {
        }
    }

    @Override
    public MessageQueue<OAIEntry> getQueue() {
        return QUEUE;
    }
}
