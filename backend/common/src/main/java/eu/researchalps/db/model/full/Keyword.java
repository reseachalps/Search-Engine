package eu.researchalps.db.model.full;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

/**
 * keywords used in the tag cloud of a structure
 */
public class Keyword {
    /**
     * Keyword for the tag cloud (most frequent word associated to a stem)
     */
    public String keyword;
    /**
     * score of this keyword (see elasticsearch TermVector score)
     */
    public float score;
    /**
     * Number of occurences of this word on analyzed resources
     */
    public long occurences;

    public Keyword(String keyword, float score, long occurences) {
        this.keyword = keyword;
        this.score = score;
        this.occurences = occurences;
    }
}
