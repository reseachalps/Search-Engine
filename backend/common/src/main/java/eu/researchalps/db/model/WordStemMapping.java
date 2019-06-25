package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashMap;
import java.util.Map;

/**
 * For a structure, stores the mapping between stem and most frequent corresponding word
 */
@Document
public class WordStemMapping {

    /**
     * Id of the structure.
     */
    @Id
    private String id;

    /**
     * mapping between stem and most frequent corresponding word
     */
    protected Map<String, String> stemToWord = new HashMap<>();

    protected byte[] vector;

    public WordStemMapping() {
    }

    public WordStemMapping(String id, Map<String, String> stemToWord, byte[] vector) {
        this.id = id;
        this.stemToWord = stemToWord;
        this.vector = vector;
    }

    public String mapStem(String stem) {
        return stemToWord.getOrDefault(stem, stem);
    }

    public Map<String, String> getStemToWord() {
        return stemToWord;
    }

    public byte[] getVector() {
        return vector;
    }

    public int stemSize() {
        return stemToWord.size();
    }
}
