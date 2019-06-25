package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

/**
 * Structures implicated in a project.
 * It can be either
 * <ul>
 * <li>an identified structure and the id (siren or RNSR structure) is provided</li>
 * <li>an external structure, id is null but label and url is provided</li>
 * </ul>
 */
public class ProjectStructure {
    /**
     * scanr id of the project structure
     */
    private String id;
    /**
     * label of the project structure if this structure is not in scanr
     */
    private String label;
    /**
     * url of the project structure if this structure is not in scanr
     */
    private String url;

    public ProjectStructure() {
    }

    public ProjectStructure(String id, String label, String url) {
        this.id = id;
        this.label = label;
        this.url = url;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }

    public boolean isExternal() {
        return id == null;
    }
}
