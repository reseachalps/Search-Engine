package eu.researchalps.db.model;

import java.util.List;

/**
 * Created by loic on 23/10/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class Name {
    private String label;
    private List<Source> sources;

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }
}
