/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.search.model;

import eu.researchalps.db.model.Address;
import eu.researchalps.util.GeoUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldIndex;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.GeoPointField;
import org.springframework.data.elasticsearch.core.geo.GeoPoint;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AddressIndex {
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String postcode;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String city;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String departement;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String country;

    @GeoPointField
    private GeoPoint gps;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String urbanUnit;

    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String nuts1;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String nuts2;
    @Field(type = FieldType.String, index = FieldIndex.not_analyzed)
    private String nuts3;


    public AddressIndex() {
    }

    public AddressIndex(Address address) {
        this.postcode = address.getPostcode();
        this.city = address.getCity();
        if (address.getGps() != null)
            this.gps = new GeoPoint(address.getGps().getY(), address.getGps().getX());
        this.urbanUnit = address.getUrbanUnit();
        this.departement = GeoUtils.secondLevelFromPostcode(address.getCitycode() == null ? address.getPostcode() : address.getCitycode());
        this.country = address.getCountry();
        final Address.NUTS nuts = address.getNuts();
        if (nuts != null) {
            nuts1 = nuts.getNuts1();
            nuts2 = nuts.getNuts2();
            nuts3 = nuts.getNuts3();
        }
    }

    public void setGps(GeoPoint gps) {
        this.gps = gps;
    }

    public String getPostcode() {
        return postcode;
    }

    public String getCity() {
        return city;
    }

    public GeoPoint getGps() {
        return gps;
    }

    public String getUrbanUnit() {
        return urbanUnit;
    }

    public String getDepartement() {
        return departement;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getNuts1() {
        return nuts1;
    }

    public String getNuts2() {
        return nuts2;
    }

    public String getNuts3() {
        return nuts3;
    }
}
