package eu.researchalps.db.model.publication;

/**
 * Structure referenced in a publication.
 */
public class PublicationStructure {
    /**
     * label
     */
    private String label;
    /**
     * acronym
     */
    private String acronym;
    /**
     * UMR 5612 for instance, this should be normalized (not leading zeros and unique spacing)
     */
    private String code;
    /**
     * RNSR code if known
     */
    private String id;

    public PublicationStructure() {
    }

    public PublicationStructure(String label, String acronym, String code, String id) {
        this.label = label;
        this.acronym = acronym;
        this.code = code;
        this.id = id;
    }

    public String getLabel() {
        return label;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getCode() {
        return code;
    }

    public String getId() {
        return id;
    }
}
