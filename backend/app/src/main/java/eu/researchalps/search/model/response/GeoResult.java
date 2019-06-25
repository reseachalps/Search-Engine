package eu.researchalps.search.model.response;

import eu.researchalps.search.model.FullStructureIndex;

/**
 * Created by loic on 26/11/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class GeoResult extends GeoBoxResult {
    public String label;
    public int projects;
    public int publications;
    public int children;
    public int linked;

    public GeoResult() {
    }

    public GeoResult(FullStructureIndex index) {
        super(index);
        this.label = index.getLabel();
        this.projects = index.getProjectsCount();
        this.publications = index.getPublicationsCount();
        this.children = index.getChildrenCount();
        this.linked = index.getGraphCount();
    }
}
