package eu.researchalps.db.model.full;

import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.StructureKind;

import java.util.List;
import java.util.Objects;

/**
 * Created by loic on 16/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class LightStructure {
    private String id;

    private StructureKind kind;

    private String label;

    private String logo;

    private List<String> acronym;

    public Boolean isPublic;

    public LightStructure() {
    }

    public LightStructure(Structure s) {
        this(s.getId(), s.getKind(), s.getLabel(), s.getLogo(), s.getAcronyms(), s.getType().getPublicEntity());
    }

    private LightStructure(String id, StructureKind kind, String label, String logo, List<String> acronym, Boolean isPublic) {
        this.id = id;
        this.kind = kind;
        this.label = label;
        this.logo = logo;
        this.acronym = acronym;
        this.isPublic = isPublic;
    }

    public String getId() {
        return id;
    }

    public StructureKind getKind() {
        return kind;
    }

    public String getLabel() {
        return label;
    }

    public String getLogo() {
        return logo;
    }

    public List<String> getAcronym() {
        return acronym;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LightStructure that = (LightStructure) o;
        return Objects.equals(id, that.id) &&
                Objects.equals(label, that.label) &&
                Objects.equals(logo, that.logo) &&
                Objects.equals(acronym, that.acronym);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, logo, acronym);
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }
}
