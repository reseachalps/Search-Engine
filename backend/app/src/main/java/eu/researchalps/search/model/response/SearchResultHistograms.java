package eu.researchalps.search.model.response;
/*
* Copyright (C) by Data Publica, All Rights Reserved.
*/

import java.util.Collection;
import java.util.LinkedHashMap;

public class SearchResultHistograms {

    private SearchResultHistogram nuts = new SearchResultHistogram();
    private SearchResultHistogram countries = new SearchResultHistogram();
    private SearchResultHistogram publicEntity = new SearchResultHistogram();

    private SearchResultHistogram projects = new SearchResultHistogram();
    private SearchResultHistogram publications = new SearchResultHistogram();
    private SearchResultHistogram connections = new SearchResultHistogram();
    private SearchResultHistogram leaders = new SearchResultHistogram();
    private SearchResultHistogram sources = new SearchResultHistogram();


    public SearchResultHistogram getCountries() {
        return countries;
    }

    public SearchResultHistogram getPublicEntity() {
        return publicEntity;
    }

    public SearchResultHistogram getProjects() {
        return projects;
    }

    public SearchResultHistogram getPublications() {
        return publications;
    }

    public SearchResultHistogram getConnections() {
        return connections;
    }

    public SearchResultHistogram getLeaders() {
        return leaders;
    }

    public SearchResultHistogram getNuts() {
        return nuts;
    }

    public SearchResultHistogram getSources() {
        return sources;
    }

    public class SearchResultHistogram {
        private long notAvailableCount = 0;
        private LinkedHashMap<String, Bin> bins = new LinkedHashMap<>();

        private SearchResultHistogram(Enum[] values) {
            for (Enum val : values) {
                bins.put(val.name(), new Bin(val.name()));
            }
        }


        private SearchResultHistogram() {
        }

        public void setBin(String key, long count) {
            setBin(key, key, count);
        }

        public void setBin(String key, String label, long count) {
            Bin bin = bins.computeIfAbsent(key, k -> new Bin(k, label));
            bin.count = count;
        }

        /**
         * @param key
         * @param count
         */
        public void addBin(String key, String label, long count) {
            Bin bin = new Bin(key, label);
            bin.count = count;
            bins.put(key, bin);
        }

        public void addBin(String key, Bin bin) {
            bins.put(key, bin);
        }

        public void setNotAvailableCount(Long notAvailableCount) {
            this.notAvailableCount = notAvailableCount;
        }

        public Collection<Bin> getBins() {
            return bins.values();
        }

        public long getNotAvailableCount() {
            return notAvailableCount;
        }
    }

    public static class Bin {
        public String key;
        public String label;
        public long count;

        private Bin(String key) {
            this(key, key);
        }

        private Bin(String key, String label) {
            this.key = key;
            this.count = 0;
            this.label = label;
        }
    }

    public static class MapBin extends Bin {
        public String svg;
        public double centerX;
        public double centerY;

        public MapBin(String key, String label, String svg, double centerX, double centerY) {
            super(key, label);
            this.svg = svg;
            this.centerX = centerX;
            this.centerY = centerY;
        }
    }
}
