package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

/**
 * this class represents the lab from which a comapny is issued
 */
public class SpinoffFrom {
    /**
     * id of the originating lab (RNSR id)
     */
    private String id;
    /**
     * Label of the originating lab
     */
    private String label;

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }
}
