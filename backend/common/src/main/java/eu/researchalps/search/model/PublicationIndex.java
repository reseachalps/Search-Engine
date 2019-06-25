package eu.researchalps.search.model;
/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.publication.Publication;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class PublicationIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;


    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String title;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String subtitle;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private List<String> authors = new LinkedList<>();

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String summary;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String alternativeSummary;


    public PublicationIndex(Publication publication) {
        this.id = publication.getId();
        this.title = publication.getTitle();
        this.subtitle = publication.getSubtitle();
        this.summary = publication.getSummary();
        this.alternativeSummary = publication.getAlternativeSummary();
        if (publication.getAuthors() != null)
            this.authors = publication.getAuthors().stream().map(a -> (a.getFirstName() != null ? a.getFirstName() + " " : "") + a.getLastName()).collect(Collectors.toList());
    }

    public PublicationIndex() {
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubtitle() {
        return subtitle;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public String getSummary() {
        return summary;
    }

    public String getAlternativeSummary() {
        return alternativeSummary;
    }
}
