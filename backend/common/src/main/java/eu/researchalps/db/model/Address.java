package eu.researchalps.db.model;


import eu.researchalps.util.GeoDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.geo.Point;
import org.springframework.data.mongodb.core.index.GeoSpatialIndexed;

import java.util.List;

/**
 * Created by loic on 15/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class Address {
    private String address;
    private String postcode;
    private String city;
    private String citycode;
    @GeoSpatialIndexed
    @JsonDeserialize(using = GeoDeserializer.class)
    private Point gps;
    private String urbanUnit;
    private String country;
    private String countryCode;
    private NUTS nuts;
    /**
     * Source information
     */
    private List<Source> sources;

    public Address() {
    }

    public Address(String postcode, String city, Point gps, String urbanUnit) {
        this.postcode = postcode;
        this.city = city;
        this.gps = gps;
        this.urbanUnit = urbanUnit;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }

    public Point getGps() {
        return gps;
    }

    public String getUrbanUnit() {
        return urbanUnit;
    }

    public String getAddress() {
        return address;
    }

    public void setGps(Point gps) {
        this.gps = gps;
    }

    public String getCitycode() {
        return citycode;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    public NUTS getNuts() {
        return nuts;
    }

    public void setNuts(NUTS nuts) {
        this.nuts = nuts;
    }

    public static class NUTS {
        private String nuts1;
        private String nuts2;
        private String nuts3;

        public String getNuts1() {
            return nuts1;
        }

        public void setNuts1(String nuts1) {
            this.nuts1 = nuts1;
        }

        public String getNuts2() {
            return nuts2;
        }

        public void setNuts2(String nuts2) {
            this.nuts2 = nuts2;
        }

        public String getNuts3() {
            return nuts3;
        }

        public void setNuts3(String nuts3) {
            this.nuts3 = nuts3;
        }
    }
}
