package eu.researchalps.crawl;

/**
 * Created by loic on 16/04/15.
 */
public class CrawlText {
    private String websiteId;
    private String title;
    private String content;
    private String lang;

    public CrawlText(String title, String content, String lang) {
        this.title = title;
        this.content = content;
        this.lang = lang;
    }

    public CrawlText(String title, String content, String lang, String websiteId) {
        this(title, content, lang);
        this.websiteId = websiteId;
    }

    public String getContent() {
        return content;
    }

    public String getLang() {
        return lang;
    }

    public String getTitle() {
        return title;
    }

    public String getWebsiteId() {
        return websiteId;
    }
}
