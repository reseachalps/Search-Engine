package eu.researchalps.db.model;

/**
 * Represent different badges associated with a structures: hdr, anr, ...
 * code is used for indexation and generate image name.
 * label is used for display name
 */
public class Badge {
    private String code;
    /**
     * label of the activity
     */
    private String label;

    public Badge(String code, String label) {
        this.code = code;
        this.label = label;
    }

    public Badge() {
    }

    public String getCode() {
        return code;
    }


    public String getLabel() {
        return label;
    }

}
