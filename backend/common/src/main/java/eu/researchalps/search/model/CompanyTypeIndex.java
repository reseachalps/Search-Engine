package eu.researchalps.search.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.CompanyType;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;

public class CompanyTypeIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String code;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String label;

    public CompanyTypeIndex() {
    }

    public CompanyTypeIndex(CompanyType companyType) {
        this.code = companyType.getId();
        this.label = companyType.getLabel();
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
