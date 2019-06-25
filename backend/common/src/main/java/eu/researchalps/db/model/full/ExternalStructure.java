package eu.researchalps.db.model.full;

/**
 * External structure referenced in a project
 */
public class ExternalStructure {
    private String label;

    private String url;


    public ExternalStructure(String label, String url) {
        this.label = label;
        this.url = url;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }
}
