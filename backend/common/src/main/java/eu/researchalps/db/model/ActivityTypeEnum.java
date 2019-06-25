package eu.researchalps.db.model;

/**
 * activity types
 */

public enum ActivityTypeEnum {
    NAF("NAF"), DOMAINE("Domaine scientifique"), ERC("Discipline ERC"), THEME("Thème");

    private String label;

    ActivityTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
