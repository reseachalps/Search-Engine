package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import java.util.Date;

/**
 * Structure financial information for Companies (public and private!)
 */
public class PrivateStructureFinance {
    /**
     * Date of the information
     */
    private Date date;
    /**
     * Revenue of the structure
     */
    private Double revenue;
    /**
     * Operating income of the structure
     */
    private Double operatingIncome;
    /**
     * Number of employees
     */
    private String employes;


    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Double getRevenue() {
        return revenue;
    }

    public void setRevenue(Double revenue) {
        this.revenue = revenue;
    }

    public Double getOperatingIncome() {
        return operatingIncome;
    }

    public void setOperatingIncome(Double operatingIncome) {
        this.operatingIncome = operatingIncome;
    }

    public String getEmployes() {
        return employes;
    }

    public void setEmployes(String employes) {
        this.employes = employes;
    }
}
