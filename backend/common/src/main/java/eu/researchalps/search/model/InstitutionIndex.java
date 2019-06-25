package eu.researchalps.search.model;

import eu.researchalps.db.model.Institution;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

/**
 * Relation to institution
 * <p>
 * The name of the relation is "code"
 * <p>
 * Created by loic on 15/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class InstitutionIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String id;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String label;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String acronym;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String code; // UMR...

    public InstitutionIndex() {
    }

    public InstitutionIndex(Institution source) {
        this.id = source.getId();
        this.label = source.getLabel();
        this.acronym = source.getAcronym();
        Institution.AssociationCode code = source.getCode();
        if (code != null) {
            this.code = code.getNormalized();
        }
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getCode() {
        return code;
    }
}
