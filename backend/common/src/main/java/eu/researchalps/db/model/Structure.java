package eu.researchalps.db.model;/*
 * Copyright (C) by Data Publica, All Rights Reserved.
 */

import eu.researchalps.util.YearFromDateDeserializer;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Une structure est le concept abstrait permettant de représenter une structure de recherche publique
 * ou une entreprise privée et publique qui sera présente dans ScanR.
 */
@Document
@CompoundIndexes({@CompoundIndex(name = "parentIdx", def = "{\"parent._id\":1}"), @CompoundIndex(name = "tags_idx", def = "{\"tags\":1}"), @CompoundIndex(name = "code_idx", def = "{\"institutions.code.normalized\":1}")})
public class Structure {

    /**
     * Identifier of the structure.
     * It can be:
     * <ul>
     *     <li>RNSR ID for research structures</li>
     *     <li>SIREN for public and private companies</li>
     * </ul>
     */
    @Id
    private String id;

    /**
     * Structure Kind : RNSR or Company
     */
    private StructureKind kind;

    /**
     * Full label of the structure (e.g. Laboratoire d'Informatique de Grenoble)
     */
    private String label;

    /**
     * Alternative commercial label for companies
     */
    private String commercialLabel;

    /**
     * Alternative labels for search (other names for research structures...)
     */
    private List<String> alias;

    /**
     * URL of the logo, can be null
     */
    private String logo;

    /**
     * Acronym of the structure
     */
    @JsonAlias({"alternative_names"})
    private List<Name> alternativeNames;

    /**
     * Acronym of the structure
     */
    private List<String> acronyms;

    /**
     * Parent structure reference
     */
    private List<ParentReference> parent;

    /**
     * Children structure references
     */
    private List<ParentReference> children;

    /**
     * History of the research structure (merge and name changes)
     */
    private List<StructureHistory> history;

    /**
     * Structure type
     * (e.g. for public structures: Unité de recherche mixte, Centre de Recherche; for private structures: PME, Grand Groupes)
     */
    private StructureType type;

    /**
     * (Private only) Detailed structure types for companies (e.g. 5400 / Société à responsabilité limitée)
     */
    private CompanyType companyType;

    /**
     * (Public only) Structure mixte
     */
    private String nature;

    /**
     * (Public only) Structure Level (1, 2, 3)
     */
    private Integer level;

    /**
     * Address block
     */
    private Address address;

    @JsonProperty("extra_fields")
    private List<Identifier> extra;

    /**
     * Creation Year of the structure
     */
    @JsonDeserialize(using = YearFromDateDeserializer.class)
    private Integer creationYear;

    /**
     * Links of the structures
     */
    private List<Link> links;

    /**
     * (Public only) Related institutions aka Tutelles
     */
    private List<Institution> institutions;

    /**
     * List of all leading people of the structure
     */
    private List<Person> leaders;

    /**
     * List of all leading staff of the structure
     */
    private List<Person> staff;

    /**
     * All foreign structural relations with this structure (e.g. COMUE, École Doctorales...)
     */
    private List<Relation> relations;

    /**
     * All activity labels that are affected to this structure (with or without a nomenclature)
     */
    private List<Activity> activities;

    /**
     * (Public only) Financial and statistical data about the structure
     */
    private StructureFinance finance;

    /**
     * (Private only) Financial and statistical data about private structure
     */
    private PrivateStructureFinance financePrivate;

    /**
     * List of tags (HDR, iLab...)
     * Tags are associated with symbols and displayed in the front end
     * @deprecated now use badges (tags will be removed)
     */
    @Deprecated
    @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    private List<String> tags;

    /**
     * List of badges (HDR, iLab...)
     * Badges are associated with symbols and displayed in the front end
     */
    private List<Badge> badges;

    /**
     * List of spinoff of this RNSR lab
     */
    private List<Spinoff> spinoffs;


    /**
     * If this structure is a spinoff of an RNSR lab, description of this lab.
     */
    private List<SpinoffFrom> spinoffFrom;

    @CreatedDate
    private Date createdDate;
    @LastModifiedDate
    private Date lastUpdated;

    /**
     * Source information
     */
    private List<Source> sources;

    /**
     * Potential identifiers
     */
    private Set<Identifier> identifiers = new HashSet<>();

    public Structure() {
    }

    public Structure(String id, StructureKind kind, String label, StructureType type) {
        this.id = id;
        this.kind = kind;
        this.label = label;
        this.type = type;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

    public List<String> getAlias() {
        return alias;
    }

    public StructureType getType() {
        return type;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public StructureKind getKind() {
        return kind;
    }

    public List<String> getAcronyms() {
        return acronyms;
    }

    public List<ParentReference> getParent() {
        return parent;
    }

    public List<StructureHistory> getHistory() {
        return history;
    }

    public CompanyType getCompanyType() {
        return companyType;
    }

    public String getNature() {
        return nature;
    }

    public Integer getLevel() {
        return level;
    }

    public Address getAddress() {
        return address;
    }

    public Integer getCreationYear() {
        return creationYear;
    }

    public List<Link> getLinks() {
        return links;
    }

    public List<Institution> getInstitutions() {
        return institutions;
    }

    public List<Person> getLeaders() {
        return leaders;
    }

    public List<Relation> getRelations() {
        return relations;
    }

    public List<Activity> getActivities() {
        return activities;
    }

    public StructureFinance getFinance() {
        return finance;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setAlias(List<String> alias) {
        this.alias = alias;
    }

    public void setAcronyms(List<String> acronyms) {
        this.acronyms = acronyms;
    }

    public void setParent(List<ParentReference> parent) {
        this.parent = parent;
    }

    public void setHistory(List<StructureHistory> history) {
        this.history = history;
    }

    public void setType(StructureType type) {
        this.type = type;
    }

    public void setCompanyType(CompanyType companyType) {
        this.companyType = companyType;
    }

    public void setNature(String nature) {
        this.nature = nature;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public void setCreationYear(Integer creationYear) {
        this.creationYear = creationYear;
    }

    public void setLinks(List<Link> links) {
        this.links = links;
    }

    public void setInstitutions(List<Institution> institutions) {
        this.institutions = institutions;
    }

    public void setLeaders(List<Person> leaders) {
        this.leaders = leaders;
    }

    public void setRelations(List<Relation> relations) {
        this.relations = relations;
    }

    public void setActivities(List<Activity> activities) {
        this.activities = activities;
    }

    public void setFinance(StructureFinance finance) {
        this.finance = finance;
    }

    public List<Spinoff> getSpinoffs() {
        return spinoffs;
    }

    public void setSpinoffs(List<Spinoff> spinoffs) {
        this.spinoffs = spinoffs;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public void setKind(StructureKind kind) {
        this.kind = kind;
    }

    public PrivateStructureFinance getFinancePrivate() {
        return financePrivate;
    }

    public void setFinancePrivate(PrivateStructureFinance financePrivate) {
        this.financePrivate = financePrivate;
    }

    public String getCommercialLabel() {
        return commercialLabel;
    }

    public void setCommercialLabel(String commercialLabel) {
        this.commercialLabel = commercialLabel;
    }

    public List<SpinoffFrom> getSpinoffFrom() {
        return spinoffFrom;
    }

    public List<Badge> getBadges() {
        if (badges == null && tags != null) {
            return tags.stream().filter(Objects::nonNull).map(t -> new Badge(t, t)).collect(Collectors.toList());
        } else
            return badges;
    }

    public void setBadges(List<Badge> badges) {
        this.badges = badges;
    }

    public List<Source> getSources() {
        return sources;
    }

    public void setSources(List<Source> sources) {
        this.sources = sources;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Structure structure = (Structure) o;
        return Objects.equals(id, structure.id) &&
                Objects.equals(lastUpdated, structure.lastUpdated);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, lastUpdated);
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }

    public List<ParentReference> getChildren() {
        return children;
    }

    public void setChildren(List<ParentReference> children) {
        this.children = children;
    }

    public List<Person> getStaff() {
        return staff;
    }

    public void setStaff(List<Person> staff) {
        this.staff = staff;
    }

    public List<Identifier> getExtra() {
        return extra;
    }

    public void setExtra(List<Identifier> extra) {
        this.extra = extra;
    }

    public List<Name> getAlternativeNames() {
        return alternativeNames;
    }

    public void setAlternativeNames(List<Name> alternativeNames) {
        this.alternativeNames = alternativeNames;
    }
}
