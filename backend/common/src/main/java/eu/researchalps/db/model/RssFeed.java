/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.db.model;

/**
 * RSS feed detected by the core extractor.
 */
public class RssFeed {

    private String url;
    private Float freq;

    public RssFeed(String url, Float freq) {
        this.url = url;
        this.freq = freq;
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Float getFreq() {
        return this.freq;
    }

    public void setFreq(Float freq) {
        this.freq = freq;
    }
}
