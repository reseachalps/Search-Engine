package eu.researchalps.workflow.structure.menesr;

import eu.researchalps.db.model.CrawlMode;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by loic on 26/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class MenesrImportServiceTest {

    @Test
    public void testInferCrawlMode() throws Exception {
        Assert.assertEquals(CrawlMode.FULL_DOMAIN, MenesrImportService.inferCrawlMode("data-publica.com"));
        Assert.assertEquals(CrawlMode.SUBPATH, MenesrImportService.inferCrawlMode("data-publica.com/stuff"));
        Assert.assertEquals(CrawlMode.SINGLE_PAGE, MenesrImportService.inferCrawlMode("data-publica.com/stuff.php"));
        Assert.assertEquals(CrawlMode.SINGLE_PAGE, MenesrImportService.inferCrawlMode("data-publica.com?id=1"));
        Assert.assertEquals(CrawlMode.SINGLE_PAGE, MenesrImportService.inferCrawlMode("data-publica.com/stuff?id=1"));
        Assert.assertEquals(CrawlMode.SINGLE_PAGE, MenesrImportService.inferCrawlMode("data-publica.com/youpi/trala/stuff?id=1"));
        Assert.assertEquals(CrawlMode.SINGLE_PAGE, MenesrImportService.inferCrawlMode("data-publica.com/youpi/trala/stuff.php"));
        Assert.assertEquals(CrawlMode.SUBPATH, MenesrImportService.inferCrawlMode("data-publica.com/youpi/tra.la/stuff"));
    }
}