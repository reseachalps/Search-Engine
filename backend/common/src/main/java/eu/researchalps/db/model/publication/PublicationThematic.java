package eu.researchalps.db.model.publication;

/**
 * Thematic of a publication
 */
public class PublicationThematic {
    /**
     * Classification nomenclature type, if any. Not bounded because no referential.
     */
    private String type;
    /**
     * Classification code (if type is specified)
     */
    private String code;
    /**
     * Classification label
     */
    private String label;

    public PublicationThematic() {
    }

    public PublicationThematic(String label) {
        this.label = label;
    }

    public PublicationThematic(String type, String code, String label) {
        this.type = type;
        this.code = code;
        this.label = label;
    }

    public String getType() {
        return type;
    }

    public String getCode() {
        return code;
    }

    public String getLabel() {
        return label;
    }
}
