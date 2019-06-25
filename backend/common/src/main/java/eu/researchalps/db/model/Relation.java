package eu.researchalps.db.model;

/**
 * Relation between structures.
 */
public class Relation {
    /**
     * Identifier of the relation (if any, e.g. ED 217)
     */
    private String id;
    /**
     * Type of the target structure (e.g. Ecole Doctorale)
     * Not bounded yet so no enum.
     */
    private RelationTypeEnum type;
    /**
     * Acronym
     */
    private String acronym;
    /**
     * Full Label
     */
    private String label;
    /**
     * Target url
     */
    private String url;

    public Relation(String id, RelationTypeEnum type, String acronym, String label, String url) {
        this.id = id;
        this.type = type;
        this.acronym = acronym;
        this.label = label;
        this.url = url;
    }

    public Relation() {
    }

    public String getId() {
        return id;
    }

    public RelationTypeEnum getType() {
        return type;
    }

    public String getAcronym() {
        return acronym;
    }

    public String getLabel() {
        return label;
    }

    public String getUrl() {
        return url;
    }
}
