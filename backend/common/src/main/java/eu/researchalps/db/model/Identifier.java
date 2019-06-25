package eu.researchalps.db.model;

import org.springframework.data.mongodb.core.index.Indexed;

import java.util.Objects;

/**
 * Created by loic on 22/05/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class Identifier {
    /**
     * enumerated value (but open type) for the type of the identifiers
     * e.g. "DOI", "HAL", "ProdINRA", "IRSTEA", "theses.fr", "OAI", "EPO", "WPO"
     */
    @Indexed
    private String type;

    /**
     * the actual ID
     */
    @Indexed
    private String id;

    /**
     * free text to describe the provenance
     */
    private String provenance;

    /**
     * link to the specific source for this publication if available
     */
    private String link;

    private boolean visible = true;

    public Identifier(String type, String id) {
        this.type = type;
        this.id = id;
    }

    public Identifier() {
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getProvenance() {
        return provenance;
    }

    public void setProvenance(String provenance) {
        this.provenance = provenance;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Identifier that = (Identifier) o;
        return Objects.equals(type, that.type) &&
                Objects.equals(id, that.id);
    }

    @Override
    public String toString() {
        return "Identifier{" +
                "type='" + type + '\'' +
                ", id='" + id + '\'' +
                ", provenance='" + provenance + '\'' +
                ", link='" + link + '\'' +
                '}';
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, id);
    }

    public boolean isVisible() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible = visible;
    }
}
