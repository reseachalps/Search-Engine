package eu.researchalps.workflow.website;

import eu.researchalps.db.model.CrawlMode;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.util.RepositoryLock;
import eu.researchalps.workflow.full.FullStructureService;
import eu.researchalps.workflow.website.publication.PublicationExtractorPlugin;
import eu.researchalps.workflow.website.screenshot.ScreenshotPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by loic on 25/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Service
public class WebsiteAnalysisService {

    private RepositoryLock<Website, String, WebsiteRepository> websiteRepository;

    @Autowired
    private StructureRepository repository;
    @Autowired
    private FullStructureService fullStructureService;
    @Autowired
    private CrawlerPlugin crawlerPlugin;
    @Autowired
    private ScreenshotPlugin screenshot;
    @Autowired
    private PublicationExtractorPlugin publicationExtractorPlugin;


    public Website analyze(String url, CrawlMode mode, boolean recrawl) {
        String id = Website.idFromUrl(url);

        return websiteRepository.update(id, tx -> {
            Website w = tx.get();
            boolean doCrawl = recrawl;
            if (w == null) {
                // new website
                w = new Website(id, url, mode);
                tx.saveDeferred(w);
                doCrawl = true;
            } else if (w.getCrawlMode() != mode) {
                doCrawl = true;
                w.setCrawlMode(mode);
                tx.saveDeferred();
            }
            if (doCrawl) {
                crawlerPlugin.execute(w);
            }
        }).getData();
    }

    public void analysisEnd(String websiteId) {
        // Ack the end of the analysis
        RepositoryLock<Website, String, WebsiteRepository>.TxResult<Boolean> result = websiteRepository.updateAndReturn(websiteId, tx -> {
            boolean isNew = tx.get().getLastCompletion() == null;
            tx.get().setLastCompletion(new Date());
            tx.saveDeferred();
            return isNew;
        });
        Website w = result.getData();

        if (result.getResult()) {
            // If the website is new
            publicationExtractorPlugin.execute(w);
            refreshFSForWebsite(w.getId(), false);
        } else {
            refreshFSForWebsite(w.getId(), true);
        }
        // Launch the screenshot
        screenshot.execute(w.getId(), w.getBaseURL());

    }

    public void refreshFSForWebsite(String websiteId, boolean delayed) {
        repository.streamAllIdsByLinkId(websiteId).forEach(id -> {
            if (delayed) {
                fullStructureService.refresh(id, FullStructureField.WEBSITES);
            } else {
                fullStructureService.delayedRefresh(id, FullStructureField.WEBSITES);
            }
        });
    }

    @Autowired
    public void setWebsiteRepository(WebsiteRepository websiteRepository) {
        this.websiteRepository = RepositoryLock.get(websiteRepository);
    }
}
