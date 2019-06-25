package eu.researchalps.search.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.CrawlMode;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

public class WebsiteIndex {

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String baseURL; // may be a domain
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private CrawlMode crawlMode;

    // WebPages are all the crawled pages of the bestWebsite associated with the
    // company
    // It is only the relevant_txt
    @Field(type = FieldType.Object)
    private List<WebPageIndex> webPages;



    public WebsiteIndex() {
    }

    public WebsiteIndex(String baseURL, CrawlMode crawlMode, List<WebPageIndex> webPages) {
        this.baseURL = baseURL;
        this.crawlMode = crawlMode;
        this.webPages = webPages;
    }

    public String getBaseURL() {
        return baseURL;
    }

    public CrawlMode getCrawlMode() {
        return crawlMode;
    }

    public List<WebPageIndex> getWebPages() {
        return webPages;
    }
}
