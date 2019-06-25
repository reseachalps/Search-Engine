package eu.researchalps.crawl;

import java.util.List;
import java.util.Map;

/**
 * Greetings traveler!
 * <p>
 * My name is: scharron
 * We are the 1/8/14, 1:44 PM
 */
public interface CrawlStoreService {
    /**
     * Return the pages crawl for a crawl id but get only the text data (used for indexation)
     *
     * @param websiteId the website id
     * @return the list of texts
     */
    List<CrawlText> getCrawlTexts(String websiteId);

    /**
     * Return the pages crawl for a crawl id but get only the text data (used for indexation)
     *
     * @param websiteId the website id
     * @return the list of texts
     */
    Map<String, List<CrawlText>> getCrawlTexts(List<String> websiteId);
}
