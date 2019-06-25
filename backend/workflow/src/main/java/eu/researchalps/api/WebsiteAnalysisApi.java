package eu.researchalps.api;

import com.datapublica.companies.workflow.service.scheduler.QueueScheduler;
import eu.researchalps.api.util.ApiConstants;
import eu.researchalps.db.model.CrawlMode;
import eu.researchalps.db.model.Link;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.db.repository.WebsiteRepository;
import eu.researchalps.workflow.structure.menesr.MenesrImportService;
import eu.researchalps.workflow.translate.TranslateJobService;
import eu.researchalps.workflow.website.CrawlerPlugin;
import eu.researchalps.workflow.website.entity.EntityExtractorPlugin;
import eu.researchalps.workflow.website.extractor.CoreExtractorPlugin;
import eu.researchalps.workflow.website.publication.PublicationExtractorPlugin;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * Created by loic on 31/03/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Controller
@RequestMapping("/services/website")
public class WebsiteAnalysisApi {
    @Autowired
    private CoreExtractorPlugin coreExtractorPlugin;

    @Autowired
    private CrawlerPlugin crawlerPlugin;

    @Autowired
    private EntityExtractorPlugin extractorPlugin;

    @Autowired
    private StructureRepository structureRepository;

    @Autowired
    private eu.researchalps.workflow.website.WebsiteAnalysisService WebsiteAnalysisService;

    @Autowired
    private PublicationExtractorPlugin publicationExtractorPlugin;

    @Autowired
    private WebsiteRepository websiteRepository;

    @Autowired
    private QueueScheduler queueScheduler;

    @Autowired
    private TranslateJobService translationService;

    @ResponseBody
    @RequestMapping(value = "/recrawl", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public long crawl() {
        return forEachWebsite(website -> crawlerPlugin.execute(website));
    }

    @ResponseBody
    @RequestMapping(value = "/extract", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public long extract() {
        return forEachWebsite(website -> coreExtractorPlugin.execute(website));
    }

    @ResponseBody
    @RequestMapping(value = "/publication", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public long publication() {
        return forEachWebsite(website -> publicationExtractorPlugin.execute(website));
    }

    @ResponseBody
    @RequestMapping(value = "/entities", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public long entityDetecter() {
        return forEachWebsite(website -> extractorPlugin.execute(website));
    }

    @ResponseBody
    @RequestMapping(value = "/translate", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public long translate() {
        return forEachWebsite(website -> translationService.enqueue(website.getId()));
    }

    @ResponseBody
    @RequestMapping(value = "/translate/{id}", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public void translateOneStructure(@PathVariable String id) {
        Structure structure = structureRepository.findOne(id);
        if (structure == null) throw new IllegalArgumentException("Cannot find structure " + id);

        List<Link> links = structure.getLinks().stream().filter(Objects::nonNull).collect(Collectors.toList());
        for (Link link : links) {
            translationService.enqueue(Website.idFromUrl(link.getUrl()));
        }
    }


    @ResponseBody
    @RequestMapping(value = "/recrawl/{id}", method = RequestMethod.POST, produces = ApiConstants.PRODUCES_JSON)
    public void crawlStructure(@PathVariable String id) {
        Structure structure = structureRepository.findOne(id);
        if (structure == null) throw new IllegalArgumentException("Cannot find structure " + id);

        List<Link> links = structure.getLinks().stream().filter(Objects::nonNull).collect(Collectors.toList());
        for (Link link : links) {
            CrawlMode mode = link.getMode();
            if (mode == null) {
                mode = MenesrImportService.inferCrawlMode(link.getId());
            }
            WebsiteAnalysisService.analyze(link.getUrl(), mode, false);
        }
    }



    private long forEachWebsite(Consumer<Website> action) {
        return websiteRepository.streamAll().peek(action).count();
    }
}
