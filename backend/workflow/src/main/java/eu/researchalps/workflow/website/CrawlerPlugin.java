package eu.researchalps.workflow.website;

import com.datapublica.companies.workflow.MessageQueue;
import com.datapublica.companies.workflow.service.QueueComponent;
import com.datapublica.companies.workflow.service.QueueListener;
import eu.researchalps.db.model.CrawlMode;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.website.extractor.CoreExtractorPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * Created by loic on 26/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Component
public class CrawlerPlugin extends QueueComponent implements QueueListener<CrawlerPlugin.CrawlResponse> {
    public static final MessageQueue<CrawlOrder> QUEUE_IN = MessageQueue.get("CRAWLER", CrawlOrder.class);
    public static final MessageQueue<CrawlResponse> QUEUE_OUT = MessageQueue.get("CRAWLER_OUT", CrawlResponse.class);

    private RepositoryLock<Website, String, WebsiteRepository> repository;

    @Autowired
    private WebsiteAnalysisService service;

    @Autowired
    private CoreExtractorPlugin coreExtractorPlugin;


    public void execute(Website c) {
        final CrawlOrder in = new CrawlOrder(c.getId(), Collections.singletonList(c.getBaseURL()), c.getCrawlMode());
        queueService.push(in, QUEUE_IN, QUEUE_OUT);
    }

    @Override
    public void receive(CrawlResponse out) {
        Website website = repository.update(out.id, tx -> {
            Website w = tx.getNotNull();
            w.setPageCount(out.count_page);
            tx.saveDeferred();
        }).getData();

        route(website);
    }

    private void route(Website website) {
        coreExtractorPlugin.execute(website);
    }

    @Override
    public MessageQueue<CrawlResponse> getQueue() {
        return QUEUE_OUT;
    }

    @Autowired
    public void setRepository(WebsiteRepository repository) {
        this.repository = RepositoryLock.get(repository);
    }

    public static class CrawlOrder {
        public String id;
        public List<String> urls;
        public String mode;

        public CrawlOrder(String id, List<String> urls, CrawlMode crawlMode) {
            this.id = id;
            this.urls = urls;
            switch (crawlMode) {
                case SINGLE_PAGE:
                    mode = "single";
                    break;
                case SUBPATH:
                    mode = "subpath";
                    break;
                case FULL_DOMAIN:
                    mode = "entire";
                    break;
            }
        }

        public CrawlOrder() {
        }
    }

    public static class CrawlResponse {
        public String id;
        public int count_page;
        public List<String> urls;
    }

}
