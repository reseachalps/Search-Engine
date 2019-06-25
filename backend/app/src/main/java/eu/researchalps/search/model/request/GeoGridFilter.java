package eu.researchalps.search.model.request;


import org.elasticsearch.common.geo.GeoPoint;

import java.util.Objects;

public class GeoGridFilter {

    public static final int DEFAULT_PRECISION = 3;
    public GeoPoint topLeft;
    public GeoPoint bottomRight;
    public int precision = DEFAULT_PRECISION;

    public GeoGridFilter() {
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GeoGridFilter that = (GeoGridFilter) o;
        return precision == that.precision &&
                Objects.equals(topLeft, that.topLeft) &&
                Objects.equals(bottomRight, that.bottomRight);
    }

    @Override
    public int hashCode() {
        return Objects.hash(topLeft, bottomRight, precision);
    }
}
