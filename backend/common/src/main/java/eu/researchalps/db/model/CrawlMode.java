package eu.researchalps.db.model;

/**
 * Crawl mode for the domain
 * <ul>
 *     <li>SINGLE_PAGE : website consisting in a single page</li>
 *     <li>SUBPATH : website consisting in a subpath of a url (all pages below)</li>
 *     <li>SINGLE_PAGE : website consisting in a whole domain</li>
 * </ul>
 */

public enum CrawlMode {
    SINGLE_PAGE, SUBPATH, FULL_DOMAIN
}
