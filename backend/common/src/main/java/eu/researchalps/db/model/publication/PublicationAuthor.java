package eu.researchalps.db.model.publication;

import java.util.List;

/**
 * Author of a publication
 */
public class PublicationAuthor {
    /**
     * first name of the author
     */
    private String firstName;
    /**
     * last name of the author
     */
    private String lastName;
    /**
     * idRef of the author
     */
    private String idref;

    private List<PublicationAffiliation> affiliations;

    public PublicationAuthor(String firstName, String lastName, List<PublicationAffiliation> affiliations) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.affiliations = affiliations;
    }

    public PublicationAuthor() {
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getIdref() {
        return idref;
    }

    public List<PublicationAffiliation> getAffiliations() {
        return affiliations;
    }

    public void setAffiliations(List<PublicationAffiliation> affiliations) {
        this.affiliations = affiliations;
    }
}
