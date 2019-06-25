package eu.researchalps.db.model.full;

import eu.researchalps.db.model.Project;
import eu.researchalps.db.model.Source;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

/**
 * Summary of a project to be included in a FullStructure
 * @see Project for the description of the content
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class FSProject {
    private String id;

    private String type;

    private String acronym;

    private String label;

    private List<LightStructure> structures;

    private List<ExternalStructure> externalStructures;

    private Integer year;

    private String budget;

    private Integer duration;

    private String url;

    private List<String> themes;

    private String callLabel;

    private String call;

    // true if project has been detected by ENR on website
    private boolean webDetected=false;

    private List<Source> sources;

    public FSProject(Project project) {
        this.id = project.getId();
        this.type = project.getType();
        this.acronym = project.getAcronym();
        this.label = project.getLabel();
        this.year = project.getYear();
        this.budget = project.getBudget();
        this.duration = project.getDuration();
        this.url = project.getUrl();
        this.themes = project.getThemes();
        this.call = project.getCall();
        this.callLabel = project.getCallLabel();
        this.sources = project.getSources();
    }

    public FSProject() {
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

    public List<LightStructure> getStructures() {
        return structures;
    }

    public void setStructures(List<LightStructure> structures) {
        this.structures = structures;
    }

    public void setExternalStructures(List<ExternalStructure> externalStructures) {
        this.externalStructures = externalStructures;
    }

    public List<ExternalStructure> getExternalStructures() {
        return externalStructures;
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

    public String getCallLabel() {
        return callLabel;
    }

    public String getCall() {
        return call;
    }

    public void setWebDetected(boolean webDetected) {
        this.webDetected = webDetected;
    }

    public boolean isWebDetected() {
        return webDetected;
    }

    public List<Source> getSources() {
        return sources;
    }
}
