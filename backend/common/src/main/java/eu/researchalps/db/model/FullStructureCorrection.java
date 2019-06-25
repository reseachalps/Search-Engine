package eu.researchalps.db.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;
import java.util.Set;

/**
 * Correction of a full struture (crowdsourcing mask)
 */
@Document
public class FullStructureCorrection {
    /**
     * id of the corresponding structure
     */
    @Id
    private String id;

    /**
     * twitter accounts
     */
    private List<String> twitterAccounts;
    /**
     * facebook accounts
     */
    private List<String> facebookAccounts;
    /**
     * activity of the structure
     */
    private String activityDescription;
    /**
     * detected publications to remove
     */
    private Set<String> removedPublications;
    /**
     * detected projects to remove
     */
    private Set<String> removedProjects;

    public FullStructureCorrection() {
    }

    public FullStructureCorrection(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public List<String> getTwitterAccounts() {
        return twitterAccounts;
    }

    public void setTwitterAccounts(List<String> twitterAccounts) {
        this.twitterAccounts = twitterAccounts;
    }

    public List<String> getFacebookAccounts() {
        return facebookAccounts;
    }

    public void setFacebookAccounts(List<String> facebookAccounts) {
        this.facebookAccounts = facebookAccounts;
    }

    public String getActivityDescription() {
        return activityDescription;
    }

    public void setActivityDescription(String activityDescription) {
        this.activityDescription = activityDescription;
    }

    public Set<String> getRemovedPublications() {
        return removedPublications;
    }

    public void setRemovedPublications(Set<String> removedPublications) {
        this.removedPublications = removedPublications;
    }

    public Set<String> getRemovedProjects() {
        return removedProjects;
    }

    public void setRemovedProjects(Set<String> removedProjects) {
        this.removedProjects = removedProjects;
    }
}
