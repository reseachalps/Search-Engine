package eu.researchalps.workflow.menesr;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.PluginService;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobInput;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobMessage;
import eu.researchalps.workflow.publication.MenesrPublicationFetcher;
import eu.researchalps.workflow.structure.menesr.MenesrImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

/**
 * Created by loic on 18/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class MenesrFetchProcess implements PluginService<MenesrFetchProcess.FetchOrder, ScheduledJobMessage<MenesrFetchProcess.FetchType, Date>> {
    public static final String PROVIDER = "menesr";
    public static final String ID_SPP = "spp";
    public static final String ID_CV = "cv";

    public static final MessageQueue<FetchOrder> QUEUE = MessageQueue.get("menesr", MenesrFetchProcess.FetchOrder.class);
    private static final Logger log = LoggerFactory.getLogger(RecrawlProcess.class);

    @Autowired
    private MenesrImportService service;
    @Autowired
    private MenesrPublicationFetcher publicationFetcher;


    @Override
    public ScheduledJobMessage<FetchType, Date> receiveAndReply(FetchOrder message) {
        switch (message.body) {
            case CV_DOI:
                log.info("Fetch DOI");
                publicationFetcher.execute();
                break;
            case STRUCTURE_PUBLICATION_PROJECT:
                log.info("Fetch Structures");
                service.fetchStructures();

                log.info("Fetch Publications");
                service.fetchPublications();

                log.info("Fetch Projects");
                service.fetchProjects();

                log.info("Finished menesr fetch");
                break;
        }
        return message;
    }

    @Override
    public MessageQueue<FetchOrder> getQueue() {
        return QUEUE;
    }

    public enum FetchType {
        STRUCTURE_PUBLICATION_PROJECT,
        CV_DOI;
    }

    public static class FetchOrder extends ScheduledJobInput<FetchType, Date> {
    }
}
