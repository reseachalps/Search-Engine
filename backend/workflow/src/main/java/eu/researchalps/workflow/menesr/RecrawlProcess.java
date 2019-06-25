package eu.researchalps.workflow.menesr;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.PluginService;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobInput;
import com.datapublica.companies.workflow.service.scheduler.ScheduledJobMessage;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.workflow.website.CrawlerPlugin;
import eu.researchalps.workflow.website.WebsiteAnalysisService;
import eu.researchalps.workflow.website.publication.PublicationExtractorPlugin;
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
public class RecrawlProcess extends QueueComponent implements PluginService<RecrawlProcess.RecrawlOrder, ScheduledJobMessage<String, Date>> {
    public static final String PROVIDER = "crawl";
    public static final String ID = "all";
    public static final String ID_PUBLICATION = "publication";
    public static final MessageQueue<RecrawlOrder> QUEUE = MessageQueue.get("WEBSITE_RECRAWL_ALL", RecrawlOrder.class);

    private static final Logger log = LoggerFactory.getLogger(RecrawlProcess.class);

    @Autowired
    private CrawlerPlugin crawlerPlugin;

    @Autowired
    public WebsiteAnalysisService websiteAnalysisService;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private PublicationExtractorPlugin publicationExtractorPlugin;


    @Override
    public ScheduledJobMessage<String, Date> receiveAndReply(RecrawlOrder message) {
        if (message.body.equals(ID))
            websiteRepository.streamAll().forEach(website -> crawlerPlugin.execute(website));
        else
            websiteRepository.streamAll().forEach(website -> publicationExtractorPlugin.execute(website));
        return message;
    }

    @Override
    public MessageQueue<RecrawlOrder> getQueue() {
        return QUEUE;
    }

    public static class RecrawlOrder extends ScheduledJobInput<String, Date> {
    }

}
