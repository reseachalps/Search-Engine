package eu.researchalps.search.model.response;

import eu.researchalps.search.model.FullStructureIndex;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

/**
 * Created by loic on 26/11/2018.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class GeoBoxResult {
    public String id;
    public Double lat;
    public Double lon;

    public GeoBoxResult() {
    }

    public GeoBoxResult(FullStructureIndex index) {
        this.id = index.getId();
        if (index.getAddress() != null && index.getAddress().getGps() != null) {
            final GeoPoint gps = index.getAddress().getGps();
            this.lat = gps.getLat();
            this.lon = gps.getLon();
        }
    }
}
