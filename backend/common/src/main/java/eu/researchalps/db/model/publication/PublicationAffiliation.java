package eu.researchalps.db.model.publication;

/**
 * Affiliation of a publication.
 */
public class PublicationAffiliation {
    /**
     * Institution (not a structure!)
     */
    private PublicationInstitution institution;
    /**
     * The structure where the person is
     */
    private PublicationStructure structure;
    private String city;
    private String country;

    public void setInstitution(PublicationInstitution institution) {
        this.institution = institution;
    }

    public void setStructure(PublicationStructure structure) {
        this.structure = structure;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public PublicationInstitution getInstitution() {
        return institution;
    }

    public PublicationStructure getStructure() {
        return structure;
    }

    public String getCity() {
        return city;
    }

    public String getCountry() {
        return country;
    }

}
