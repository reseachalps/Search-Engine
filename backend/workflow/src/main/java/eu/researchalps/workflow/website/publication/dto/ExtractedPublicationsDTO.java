package eu.researchalps.workflow.website.publication.dto;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by loic on 02/05/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class ExtractedPublicationsDTO {
    public String id;
    public String url;
    public List<String> dois;
    public List<String> references;

    public ExtractedPublicationsDTO() {
    }

    public ExtractedPublicationsDTO(String id) {
        this.id = id;
        dois = new LinkedList<>();
        references = new LinkedList<>();
    }
}
