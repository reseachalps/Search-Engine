package eu.researchalps.db.model;

/**
 * Type of company (code/label)
 */

public class CompanyType {
    private String id;
    private String label;

    public CompanyType() {
    }

    public CompanyType(String id, String label) {
        this.id = id;
        this.label = label;
    }

    public String getId() {
        return id;
    }

    public String getLabel() {
        return label;
    }

}
