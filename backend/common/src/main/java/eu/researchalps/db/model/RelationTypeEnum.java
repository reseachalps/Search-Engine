package eu.researchalps.db.model;

/**
 * Type of relationship between structures
 */
public enum RelationTypeEnum {
    COMUE("Comue"), CARNOT("Carnot"), ECOLE_DOCTORALE("Ecole Doctorale"), POLE("Pôle de compétitivité"), INCUBATEUR("Incubateur");
    
    private String label;

    RelationTypeEnum(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
