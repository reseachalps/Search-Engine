package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.util.TolerantBooleanDeserializer;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Structure type (company, public private...)
 */
public class StructureType {
    public static final StructureType RNSR_STRUCTURE = new StructureType("rnsr", "RNSR", true);
    private String id;
    private String label;

    @JsonDeserialize(using = TolerantBooleanDeserializer.class)
    @JsonProperty("isPublic")
    private Boolean publicEntity = null;

    public StructureType() {
    }

    public StructureType(String id, String label, boolean publicEntity) {
        this.id = id;
        this.label = label;
        this.publicEntity = publicEntity;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getPublicEntity() {
        return publicEntity;
    }

    public void setPublicEntity(Boolean publicEntity) {
        this.publicEntity = publicEntity;
    }
}
