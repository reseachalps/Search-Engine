package eu.researchalps.db.model;

import java.util.Date;
import java.util.Objects;

/**
 * Source tracing data
 */
public class Source {
    private String label;
    private Date revisionDate;
    private String url;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Date getRevisionDate() {
        return revisionDate;
    }

    public void setRevisionDate(Date revisionDate) {
        this.revisionDate = revisionDate;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Source source = (Source) o;
        return Objects.equals(label, source.label) &&
                Objects.equals(revisionDate, source.revisionDate) &&
                Objects.equals(url, source.url);
    }

    @Override
    public int hashCode() {
        return Objects.hash(label, revisionDate, url);
    }
}
