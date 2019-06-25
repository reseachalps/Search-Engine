package eu.researchalps.db.model;

/**
 * Parent structure reference
 */
public class ParentReference {
    /**
     * Parent structure id
     */
    private String id;
    /**
     * Is this structure hierarchy exclusive
     */
    private boolean exclusive = true;

    public ParentReference(String id, boolean exclusive) {
        this.id = id;
        this.exclusive = exclusive;
    }

    public ParentReference() {
    }

    public ParentReference(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public boolean isExclusive() {
        return exclusive;
    }
}
