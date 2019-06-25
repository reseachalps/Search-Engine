/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.db.model;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * Event to trace when a user searches in db for audit and statistical purposed.
 */
@Document
public class SearchEvent {
    /**
     * Internal database id.
     */
    @Id
    private ObjectId id;
    /**
     * userid for this feedback (null in scanner for the moment since no user is authenticated)
     */
    @Indexed
    private String userId;
    /**
     * date of the search
     */
    @Indexed
    private Date date;

    /**
     * string query
     */
    private String query;
    /**
     * page of the query
     */
    private int page;

    /**
     * full request object
     */
    private Object extraSearchParams;

    public SearchEvent() {
    }

    public SearchEvent(String query, int page, Object extraSearchParams) {
        this(null, new Date(), query, page, extraSearchParams);
    }

    public SearchEvent(String userId, Date date, String query, int page, Object extraSearchParams) {
        this.userId = userId;
        this.date = date;
        this.query = query;
        this.page = page;
        this.extraSearchParams = extraSearchParams;
    }

    public ObjectId getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public Date getDate() {
        return date;
    }

    public String getQuery() {
        return query;
    }

    public int getPage() {
        return page;
    }

    public Object getExtraSearchParams() {
        return extraSearchParams;
    }
}