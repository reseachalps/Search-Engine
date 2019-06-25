package eu.researchalps.search.repository;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.WordStemMapping;
import eu.researchalps.db.model.full.Keyword;
import eu.researchalps.search.model.request.SearchRequest;
import eu.researchalps.search.model.response.FullStructureResult;
import eu.researchalps.search.model.response.GeoBoxResult;
import eu.researchalps.search.model.response.GeoResult;
import eu.researchalps.search.model.response.SearchResponse;
import org.elasticsearch.common.geo.GeoPoint;

import java.io.IOException;
import java.util.List;

public interface FullStructureSearchRepositoryCustom {
    /**
     * Return the results for the request
     *
     * @param searchRequest
     * @return
     * @throws IOException
     */
    SearchResponse searchFullStructureWithQuery(SearchRequest searchRequest) throws IOException;

    List<FullStructureResult> getFirstFullStructureWithQuery(SearchRequest request, int size) throws IOException;

    /**
     * Return the results for the request
     *
     * @param searchRequest
     * @return
     * @throws IOException
     */
    List<FullStructureResult> getAllFullStructureWithQuery(SearchRequest searchRequest) throws IOException;

    /**
     * Return the results for the request for geo display.
     * Returns all the results (no paging), no histograms, gps infos.
     *
     * @param searchRequest
     * @return
     * @throws IOException
     */
    SearchResponse geoElementsWithQuery(SearchRequest searchRequest) throws IOException;

    List<GeoBoxResult> geoBoxResults(GeoPoint topLeft, GeoPoint bottomRight, SearchRequest searchRequest) throws IOException;
    List<GeoResult> geoResults(double lat, double lon, String radius, SearchRequest searchRequest) throws IOException;
    List<GeoResult> geoResults(List<GeoPoint> polygon, SearchRequest searchRequest) throws IOException;
    GeoResult findOneGeo(String id);
    List<GeoResult> fastSearch(String query, int size);

    List<Keyword> computeWordCloud(String id, WordStemMapping mapping) throws IOException;
    List<Keyword> computeWordCloud(SearchRequest request) throws IOException;
}
