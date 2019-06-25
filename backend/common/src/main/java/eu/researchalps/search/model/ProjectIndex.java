package eu.researchalps.search.model;
/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.full.FSProject;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class ProjectIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String acronym;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text")
    private String label;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text", searchAnalyzer = "text")
    private String description;

    @Field(type = FieldType.String, index = FieldIndex.analyzed, analyzer = "text")
    private String callLabel;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String call;


    public ProjectIndex(FSProject project) {
        this.id = project.getId();
        this.acronym = project.getAcronym();
        this.label = project.getLabel();
        this.call = project.getCall();
        this.callLabel = project.getCallLabel();
    }

    public ProjectIndex() {
    }

    public String getId() {
        return id;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    public String getCallLabel() {
        return callLabel;
    }

    public String getCall() {
        return call;
    }
}
