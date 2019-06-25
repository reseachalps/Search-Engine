package eu.researchalps.db.model.publication;

/**
 * Collection of the publication.
 */
public class PublicationSourceCollection {
    /**
     * Collection title
     */
    private String title;
    /**
     * ISSN number
     */
    private String issn;
    /**
     * The number of the issue, this is typically of the format "9 (1)" (first article of the 9th volume)
     */
    private String issue;

    public PublicationSourceCollection() {
    }

    public PublicationSourceCollection(String title, String issn, String issue) {
        this.title = title;
        this.issn = issn;
        this.issue = issue;
    }

    public String getTitle() {
        return title;
    }

    public String getIssn() {
        return issn;
    }

    public String getIssue() {
        return issue;
    }
}
