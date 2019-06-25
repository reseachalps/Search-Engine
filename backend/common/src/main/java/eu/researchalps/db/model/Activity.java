package eu.researchalps.db.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Represent different types of activity descriptions.
 * <ul>
 * <li>
 * NAF
 * </li>
 * <li>
 * ERC - (secondary are possible)
 * </li>
 * <li>
 * Domains
 * </li>
 * <li>
 * (nothing) Champs libres
 * </li>
 * </ul>
 * <p>
 * Indexation:
 * <ul>
 * <li> for coded nomenclatures (NAF, ERC, Domains), use a facet by Type</li>
 * <li> for all nomenclatures stores the labels</li>
 * </ul>
 * Created by loic on 15/02/2016.
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
public class Activity {
    private static final Logger log = LoggerFactory.getLogger(Activity.class);
    /**
     * code of the activity
     */
    private String code;
    /**
     * type of the activity
     */
    private ActivityTypeEnum activityType;
    /**
     * lable of the activity
     */
    private String label;
    /**
     * true for secondary activities
     */
    private Boolean secondary;

    public Activity(String code, ActivityTypeEnum activityType, String label, Boolean secondary) {
        this.code = code;
        this.activityType = activityType;
        this.label = label;
        this.secondary = secondary;
    }

    public Activity() {
    }

    public String getCode() {
        return code;
    }

    public ActivityTypeEnum getActivityType() {
        return activityType;
    }

    public String getLabel() {
        return label;
    }

    public Boolean getSecondary() {
        return secondary;
    }

    @JsonProperty
    public void setType(String activityTypeAsString) {
        if (activityTypeAsString == null) return;
        //TODO: remove this mapping
        switch (activityTypeAsString) {
            case "Domaine":
                this.activityType = ActivityTypeEnum.DOMAINE;
                break;
            case "ERC":
                this.activityType = ActivityTypeEnum.ERC;
                break;
            case "Thème de recherche":
                this.activityType = ActivityTypeEnum.THEME;
                break;
            case "NAF":
                this.activityType = ActivityTypeEnum.NAF;
                break;
            default:
                log.warn("Cannot map activityType :" + activityType);
        }
    }
}
