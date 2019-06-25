package eu.researchalps.search.model;

import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Represents a crawled webpage and its pieces of information
 *
 * @author glebourg
 */
public class WebPageIndex {
    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String title;
    // This content is now filled with relevant text, and not a stripped html page
    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String content;

    public WebPageIndex() {
    }

    public WebPageIndex(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

}
