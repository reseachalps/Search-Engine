package eu.researchalps.db.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by loic on 22/02/2019.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
@Document
public class Translation {
    @Id
    private String id;

    private Map<String, String> translations = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Map<String, String> getTranslations() {
        return translations;
    }

    public void setTranslations(Map<String, String> translations) {
        this.translations = translations;
    }
}
