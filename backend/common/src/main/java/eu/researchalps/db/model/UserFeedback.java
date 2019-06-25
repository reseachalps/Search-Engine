package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

/**
 * This class store the feedback of scanr users on structure record (crowdsourcing)
 */
@Document
public class UserFeedback {
    /**
     * Internal Id of the feedback
     */
    private String id;

    /**
     * Status of the feedback
     */
    private UserFeedbackStatus status = UserFeedbackStatus.SUBMITTED;
    /**
     * id of the structure concerned by the feedback
     */
    private String structure;
    /**
     * Name of the user
     */
    private String userName;
    /**
     * Email of the user
     */
    private String email;
    /**
     * Company record part concerned by the feedback
     */
    private String field;
    /**
     * Type of feedback
     */
    private UserFeedbackAction action;
    /**
     * Optional value for the feedback
     */
    private String value;
    /**
     * Comment of the user
     */
    private String comment;
    
    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastUpdated;

    public UserFeedback() {
    }


    public String getId() {
        return id;
    }

    public UserFeedbackStatus getStatus() {
        return status;
    }

    public String getUserName() {
        return userName;
    }

    public String getEmail() {
        return email;
    }

    public String getField() {
        return field;
    }

    public String getComment() {
        return comment;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public UserFeedbackAction getAction() {
        return action;
    }

    public String getStructure() {
        return structure;
    }

    public String getValue() {
        return value;
    }

    public void markProcessed() {
        status = UserFeedbackStatus.PROCESSED;
    }
}
