/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.search.model.response;

import eu.researchalps.search.model.request.SearchRequest;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.Collection;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class SearchResponse {

    private SearchRequest request;
    private long total;
    private Collection<FullStructureResult> results;
    private SearchResultHistograms histograms;

    public SearchResponse(SearchRequest request, long total, Collection<FullStructureResult> results) {
        this.request = request;
        this.total = total;
        this.results = results;
    }

    public SearchRequest getRequest() {
        return request;
    }

    public long getTotal() {
        return total;
    }

    public Collection<FullStructureResult> getResults() {
        return results;
    }

    public SearchResultHistograms getHistograms() {
        return histograms;
    }

    public void setHistograms(SearchResultHistograms histograms) {
        this.histograms = histograms;
    }
}
