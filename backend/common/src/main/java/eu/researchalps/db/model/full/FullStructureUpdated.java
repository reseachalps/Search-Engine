package eu.researchalps.db.model.full;

import java.util.Date;
import java.util.Set;

/**
 * Stores the modification to be done on a full structure (to batch the modifiations of a struture).
 */
public class FullStructureUpdated {
    private String id;
    private Date lastUpdated;
    private Set<FullStructureField> modifiedFields;

    public FullStructureUpdated() {
    }

    public FullStructureUpdated(String id, Date lastUpdated, Set<FullStructureField> modifiedFields) {
        this.id = id;
        this.lastUpdated = lastUpdated;
        this.modifiedFields = modifiedFields;
    }

    public String getId() {
        return id;
    }

    public Date getLastUpdated() {
        return lastUpdated;
    }

    public Set<FullStructureField> getModifiedFields() {
        return modifiedFields;
    }
}
