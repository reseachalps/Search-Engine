package eu.researchalps.util;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.geo.Point;

import java.io.IOException;

/**
 * Deserialize a structure such as {
 * "lat": 43.3025,
 * "lon": 5.4017
 * } in a Point[longitude, latitude] (longitude first)
 */

public class GeoDeserializer extends JsonDeserializer<Point> {

    public static final String LAT = "lat";
    public static final String LON = "lon";

    @Override
    public Point deserialize(JsonParser jp, DeserializationContext deserializationContext) throws IOException {
        JsonNode node = jp.getCodec().readTree(jp);
        if (node != null) {
            JsonNode latNode = node.get(LAT);
            JsonNode lonNode = node.get(LON);
            if (latNode != null && latNode.isNumber() && lonNode != null && lonNode.isNumber()) {
                double lat = latNode.doubleValue();
                double lon = lonNode.doubleValue();
                return new Point(lon, lat);
            }
        }
        return null;
    }

}
