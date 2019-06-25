/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */
package eu.researchalps.search.model.request;

import eu.researchalps.util.TextFiltering;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import org.springframework.data.annotation.Transient;

import java.util.List;
import java.util.Locale;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonIgnoreProperties(ignoreUnknown = true)
public class SearchRequest {
    public static final int PAGE_SIZE = 20;
    private static final int MAX_PAGE = 50;

    public enum SearchField {
        ALL, ID, NAME
    }

    //TODO: add other sort order?
    public enum SortOrder {
        RELEVANCY
    }

    public enum SortDirection {
        ASC,
        DESC
    }

    private String query;

    private int page = 1;
    private int pageSize = PAGE_SIZE;
    @Transient
    private List<Locale> locales;

    public SortOrder sortOrder = SortOrder.RELEVANCY;
    public SortDirection sortDirection = SortDirection.DESC;
    private SearchField searchField = SearchField.ALL;


    private GeoGridFilter geoGrid;

    private MultiValueSearchFilter kind;
    private MultiValueSearchFilter publicEntity;
    private MultiValueSearchFilter type;
    private MultiValueSearchFilter urbanUnit;
    private MultiValueSearchFilter departements;
    private MultiValueSearchFilter domaine;
    private MultiValueSearchFilter naf;
    private MultiValueSearchFilter erc;
    private MultiValueSearchFilter institutions; // id of the institutions
    private MultiValueSearchFilter projects; // id of the projects
    private MultiValueSearchFilter badges; // id of the projects
    private MultiValueSearchFilter calls; // id of the call
    private MultiValueSearchFilter countries;
    private MultiValueSearchFilter nuts;
    private MultiValueSearchFilter sources;
    private MultiValueSearchFilter ids;

    /**
     *
     */
    public SearchRequest() {
    }

    public String getQuery() {
        return TextFiltering.filterQuery(query);
    }

    public int getPage() {
        return Math.min(page, MAX_PAGE);
    }

    public int getPageSize() {
        return Math.min(pageSize, PAGE_SIZE);
    }

    public int getFrom() {
        return (getPage() - 1) * getPageSize();
    }


    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    @JsonIgnore
    public List<Locale> getLocales() {
        return locales;
    }

    public SortOrder getSortOrder() {
        return sortOrder;
    }

    public SortDirection getSortDirection() {
        return sortDirection;
    }

    public SearchField getSearchField() {
        return searchField;
    }

    public GeoGridFilter getGeoGrid() {
        return geoGrid;
    }

    public MultiValueSearchFilter getKind() {
        return kind;
    }

    public void setKind(MultiValueSearchFilter kind) {
        this.kind = kind;
    }

    public MultiValueSearchFilter getPublicEntity() {
        return publicEntity;
    }

    public void setPublicEntity(MultiValueSearchFilter publicEntity) {
        this.publicEntity = publicEntity;
    }

    public MultiValueSearchFilter getType() {
        return type;
    }

    public void setType(MultiValueSearchFilter type) {
        this.type = type;
    }

    public MultiValueSearchFilter getUrbanUnit() {
        return urbanUnit;
    }

    public void setUrbanUnit(MultiValueSearchFilter urbanUnit) {
        this.urbanUnit = urbanUnit;
    }

    public MultiValueSearchFilter getDomaine() {
        return domaine;
    }

    public void setDomaine(MultiValueSearchFilter domaine) {
        this.domaine = domaine;
    }

    public MultiValueSearchFilter getNaf() {
        return naf;
    }

    public void setNaf(MultiValueSearchFilter naf) {
        this.naf = naf;
    }

    public MultiValueSearchFilter getErc() {
        return erc;
    }

    public void setErc(MultiValueSearchFilter erc) {
        this.erc = erc;
    }

    public MultiValueSearchFilter getInstitutions() {
        return institutions;
    }

    public void setInstitutions(MultiValueSearchFilter institutions) {
        this.institutions = institutions;
    }

    public MultiValueSearchFilter getProjects() {
        return projects;
    }

    public MultiValueSearchFilter getCalls() {
        return calls;
    }

    public void setProjects(MultiValueSearchFilter projects) {
        this.projects = projects;
    }

    public MultiValueSearchFilter getDepartements() {
        return departements;
    }

    public void setDepartements(MultiValueSearchFilter departements) {
        this.departements = departements;
    }

    public MultiValueSearchFilter getBadges() {
        return badges;
    }

    public void setBadges(MultiValueSearchFilter badges) {
        this.badges = badges;
    }

    public MultiValueSearchFilter getNuts() {
        return nuts;
    }

    public MultiValueSearchFilter getSources() {
        return sources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SearchRequest that = (SearchRequest) o;
        return page == that.page &&
                pageSize == that.pageSize &&
                Objects.equals(query, that.query) &&
                sortOrder == that.sortOrder &&
                sortDirection == that.sortDirection &&
                searchField == that.searchField &&
                Objects.equals(geoGrid, that.geoGrid) &&
                Objects.equals(kind, that.kind) &&
                Objects.equals(publicEntity, that.publicEntity) &&
                Objects.equals(type, that.type) &&
                Objects.equals(urbanUnit, that.urbanUnit) &&
                Objects.equals(departements, that.departements) &&
                Objects.equals(domaine, that.domaine) &&
                Objects.equals(naf, that.naf) &&
                Objects.equals(erc, that.erc) &&
                Objects.equals(institutions, that.institutions) &&
                Objects.equals(projects, that.projects) &&
                Objects.equals(badges, that.badges) &&
                Objects.equals(calls, that.calls) &&
                Objects.equals(nuts, that.nuts) &&
                Objects.equals(sources, that.sources) &&
                Objects.equals(ids, that.ids) &&
                Objects.equals(countries, that.countries);
    }

    public MultiValueSearchFilter getCountries() {
        return countries;
    }

    public void setCountries(MultiValueSearchFilter countries) {
        this.countries = countries;
    }

    public MultiValueSearchFilter getIds() {
        return ids;
    }

    @Override
    public int hashCode() {
        return Objects.hash(query, page, pageSize, sortOrder, sortDirection, searchField, geoGrid, kind, publicEntity, type, urbanUnit, departements, domaine, naf, erc, institutions, projects, badges, calls, countries, nuts, sources, ids);
    }
}
