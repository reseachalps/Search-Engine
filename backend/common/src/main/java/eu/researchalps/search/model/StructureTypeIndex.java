package eu.researchalps.search.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.StructureType;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class StructureTypeIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String code;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String label;

    public StructureTypeIndex() {
    }

    public StructureTypeIndex(StructureType structureType) {
        this.code = structureType.getId();
        this.label = structureType.getLabel();
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
