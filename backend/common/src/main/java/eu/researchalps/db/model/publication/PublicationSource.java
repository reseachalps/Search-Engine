package eu.researchalps.db.model.publication;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Soruce of the publication
 */
public class PublicationSource {
    /**
     * Title of the source, may be null if part of a collection
     */
    private String title;
    private String subtitle;
    /**
     * Type of the source, if COLLECTION then collection may contain collection data
     */
    private SourceType type;
    /**
     * Collection metadata
     * aka. journal or revue or plenty of other names.
     */
    private PublicationSourceCollection collection;
    /**
     * pages information like "512-518"
     */
    private String pagination;
    /**
     * Article number inside the source as complementary info
     */
    private String articleNumber;

    @JsonProperty
    public String getAsString() {
        String result = "";
        String pages = (articleNumber != null ? articleNumber : "");
        if (pagination != null) {
            if (pages.isEmpty()) {
                pages = pagination;
            } else {
                pages += ", p" + pagination;
            }
        }
        boolean paginationAdded = false;
        if (collection != null && collection.getTitle() != null) {
            result = collection.getTitle();
            if (collection.getIssue() != null) {
                result += " " + collection.getIssue();
            }

            if (collection.getIssn() != null) {
                result += " [" + collection.getIssn()+"]";
            }

            paginationAdded = true;
            if (!pages.isEmpty()) {
                result += ", " +pages+ "";
            }
        }

        if (title != null) {
            result += " " + title;
            if (subtitle != null) {
                result += " " + subtitle;
            }
        }
        if (!paginationAdded) {
            if (!pages.isEmpty()) {
                result += ", " +pages+ "";
            }
        }
        return result.trim();
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setSubtitle(String subtitle) {
        this.subtitle = subtitle;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public void setCollection(PublicationSourceCollection collection) {
        this.collection = collection;
    }

    public void setPagination(String pagination) {
        this.pagination = pagination;
    }

    public void setArticleNumber(String articleNumber) {
        this.articleNumber = articleNumber;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public SourceType getType() {
        return type;
    }

    public PublicationSourceCollection getCollection() {
        return collection;
    }

    public String getPagination() {
        return pagination;
    }

    public String getArticleNumber() {
        return articleNumber;
    }

    public enum SourceType {
        PROCEEDINGS, EVENT, BOOK, COLLECTION, ARTICLE
    }

}
