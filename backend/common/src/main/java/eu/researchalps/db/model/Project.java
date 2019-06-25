package eu.researchalps.db.model;

import eu.researchalps.util.YearFromDateDeserializer;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Research project.
 */
@Document
@JsonInclude(JsonInclude.Include.NON_NULL)
@CompoundIndexes({@CompoundIndex(name = "structuresIdx", def = "{\"structures._id\":1}")})
public class Project {
    /**
     * id of tis project
     */
    @Id
    private String id;

    private String type;

    /**
     * project's acronym
     */
    @Indexed
    private String acronym;

    /**
     * project's label
     */
    private String label;

    /**
     * project's description
     */
    private String description;

    /**
     * project's participating structures (scanr structures or external structures)
     */
    private List<ProjectStructure> structures;

    /**
     * project's year
     */
    @JsonDeserialize(using = YearFromDateDeserializer.class)
    private Integer year;
    /**
     * project's budget in euro
     */
    private String budget;

    /**
     * project's duration in month
     */
    private Integer duration;
    /**
     * project's url
     */
    private String url;

    /**
     * project's themes (list of free text)
     */
    private List<String> themes;

    /**
     * project's call label
     */
    private String callLabel;

    /**
     * project's call id
     */
    private String call;

    /**
     * Source information
     */
    private List<Source> sources;

    /**
     * Potential identifiers
     */
    private Set<Identifier> identifiers = new HashSet<>();

    public Project(String id, String type, String acronym, String label) {
        this.id = id;
        this.type = type;
        this.acronym = acronym;
        this.label = label;
    }

    public Project() {
    }

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getLabel() {
        return label;
    }

    public List<ProjectStructure> getStructures() {
        return structures;
    }

    public void setStructures(List<ProjectStructure> structures) {
        this.structures = structures;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getBudget() {
        return budget;
    }

    public void setBudget(String budget) {
        this.budget = budget;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public List<String> getThemes() {
        return themes;
    }

    public void setThemes(List<String> themes) {
        this.themes = themes;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getCallLabel() {
        return callLabel;
    }

    public String getCall() {
        return call;
    }

    public List<Source> getSources() {
        return sources;
    }

    public Set<Identifier> getIdentifiers() {
        return identifiers;
    }

    public void setIdentifiers(Set<Identifier> identifiers) {
        this.identifiers = identifiers;
    }
}
