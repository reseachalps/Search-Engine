package eu.researchalps.db.model.publication;

/**
 * Code/Lable of the institution of a publication
 */
public class PublicationInstitution {
    private String label;
    private String acronym;

    public PublicationInstitution() {
    }

    public PublicationInstitution(String label, String acronym) {
        this.label = label;
        this.acronym = acronym;
    }

    public String getLabel() {
        return label;
    }

    public String getAcronym() {
        return acronym;
    }
}
