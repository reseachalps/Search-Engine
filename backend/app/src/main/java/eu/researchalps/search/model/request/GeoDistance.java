package eu.researchalps.search.model.request;

import org.elasticsearch.common.geo.GeoPoint;

/**
 * Created by loic on 26/11/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class GeoDistance {
    public GeoPoint center;
    public String distance;
    public SearchRequest searchRequest;
}
