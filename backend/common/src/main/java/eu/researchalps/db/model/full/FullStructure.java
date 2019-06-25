package eu.researchalps.db.model.full;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.Website;
import eu.researchalps.db.model.publication.PublicationType;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.google.common.collect.Sets;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Full structure: gather all the information (publication, projects...) of a Structure.
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FullStructure {
    @Id
    private String id;

    /**
     * Structure informations for this full structure
     */
    private Structure structure;
    /**
     * Children of this structure
     */
    private List<LightStructure> children;

    /**
     * Parents of this structure
     */
    private List<LightStructure> parents;

    /**
     * list of websites informations for the websites of this structure.
     */
    private List<Website> websites;

    /**
     * Projects attached to the structure
     */
    private List<FSProject> projects;

    /**
     * Publications attached to the structure
     */
    private List<FSPublication> publications;

    /**
     * Projects detected on the websites of this structure (ENR)
     */
    private List<FSProject> detectedProjects;

    /**
     * Publications detected on the websites of this structure (ENR)
     */
    private List<FSPublication> detectedPublications;

    /**
     * Graph Elements of this structure (relationships towards other structures)
     */
    private List<GraphElement> graph;

    @LastModifiedDate
    private Date lastUpdated;

    @JsonIgnore
    private boolean indexed;

    @JsonIgnore
    private Set<FullStructureField> fieldsToRefresh = Sets.newHashSet();

    public FullStructure() {
    }

    public FullStructure(String id) {
        this.id = id;
    }

    public FullStructure(String id, Structure structure, List<Website> websites, List<FSProject> projects) {
        this.id = id;
        this.structure = structure;
        this.websites = websites;
        this.projects = projects;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Structure getStructure() {
        return structure;
    }

    public void setStructure(Structure structure) {
        this.structure = structure;
    }

    public List<Website> getWebsites() {
        return websites;
    }

    public void setWebsites(List<Website> websites) {
        this.websites = websites;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public void setLastUpdated(Date lastUpdated) {
        this.lastUpdated = lastUpdated;
    }

    public List<FSProject> getProjects() {
        return projects;
    }

    public void setProjects(List<FSProject> projects) {
        this.projects = projects;
    }

    public List<LightStructure> getParents() {
        return parents;
    }

    public void setParents(List<LightStructure> parents) {
        this.parents = parents;
    }

    public List<LightStructure> getChildren() {
        return children;
    }

    public void setChildren(List<LightStructure> children) {
        this.children = children;
    }

    public List<GraphElement> getGraph() {
        return graph;
    }

    public void setGraph(List<GraphElement> graph) {
        this.graph = graph;
    }

    public void setPublications(List<FSPublication> publications) {
        this.publications = publications;
    }

    public List<FSProject> getDetectedProjects() {
        return detectedProjects;
    }

    public void setDetectedProjects(List<FSProject> detectedProjects) {
        this.detectedProjects = detectedProjects;
    }

    @JsonIgnore
    public List<FSPublication> getPublications() {
        return publications;
    }

    public void setDetectedPublications(List<FSPublication> detectedPublications) {
        this.detectedPublications = detectedPublications;
    }

    @JsonIgnore
    public List<FSPublication> getDetectedPublications() {
        return detectedPublications;
    }

    public List<FSPublication> getDetectedPublicationList() {
        return detectedPublications != null ? detectedPublications.stream()
                .filter(p -> !PublicationType.PATENT.equals(p.getType()) && !PublicationType.THESIS.equals(p.getType()))
//                .sorted(Comparator.nullsLast(Comparator.comparing(FSPublication::getPublicationDate).reversed()))
                .collect(Collectors.toList()) : null;
    }

    public List<FSPublication> getDetectedPatentList() {
        return detectedPublications != null ? detectedPublications.stream()
                .filter(p -> PublicationType.PATENT.equals(p.getType()))
                .collect(Collectors.toList()) : null;
    }

    public List<FSPublication> getDetectedThesisList() {
        return detectedPublications != null ? detectedPublications.stream()
                .filter(p -> PublicationType.THESIS.equals(p.getType()))
//                .sorted(Comparator.nullsLast(Comparator.comparing(FSPublication::getPublicationDate).reversed()))
                .collect(Collectors.toList()) : null;
    }


    public List<FSPublication> getPublicationList() {
        return publications != null ? publications.stream()
                .filter(p -> !PublicationType.PATENT.equals(p.getType()) && !PublicationType.THESIS.equals(p.getType()))
                .collect(Collectors.toList()) : null;
    }

    public List<FSPublication> getPatentList() {
        return publications != null ? publications.stream()
                .filter(p -> PublicationType.PATENT.equals(p.getType()))
                .collect(Collectors.toList()) : null;
    }

    public List<FSPublication> getThesisList() {
        return publications != null ? publications.stream()
                .filter(p -> PublicationType.THESIS.equals(p.getType()))
                .collect(Collectors.toList()) : null;
    }

    public boolean isIndexed() {
        return indexed;
    }

    public void setIndexed(boolean indexed) {
        this.indexed = indexed;
    }

    public Set<FullStructureField> getFieldsToRefresh() {
        return fieldsToRefresh;
    }
}
