package eu.researchalps.db.model;

import java.util.List;
import java.util.Objects;

/**
 * Link for documentatio of a structure
 */
public class Link {

    /**
     * id of the link
     */
    private String id;

    /**
     * type of link
     */
    private LinkType type;

    /**
     * url of the link
     */
    private String url;

    /**
     * label to be displayed
     */
    private String label;

    /**
     * crawl mode for links to be crawled (websites of structures)
     */
    private CrawlMode mode;
    /**
     * Source information
     */
    private List<Source> sources;

    public Link(LinkType type, String url) {
        this.type = type;
        this.url = url;
        if (url != null)
            id = Website.idFromUrl(url);
    }

    public Link() {
    }

    public LinkType getType() {
        return type;
    }

    public String getUrl() {
        return url;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public void computeId() {
        if (url == null) return;
        try {
            id = Website.idFromUrl(url);
        } catch (IllegalArgumentException ignored) {
            // id will stay null as url is not parsable...
            id = null;
        }
    }

    public CrawlMode getMode() {
        return mode;
    }

    public void setMode(CrawlMode mode) {
        this.mode = mode;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Link link = (Link) o;
        return Objects.equals(id, link.id) &&
                type == link.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type);
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
}
