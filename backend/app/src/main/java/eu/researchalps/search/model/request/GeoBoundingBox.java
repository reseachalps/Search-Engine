package eu.researchalps.search.model.request;

import org.elasticsearch.common.geo.GeoPoint;

/**
 * Created by loic on 26/11/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class GeoBoundingBox {
    public GeoPoint topLeft;
    public GeoPoint bottomRight;
    public SearchRequest searchRequest;
}
