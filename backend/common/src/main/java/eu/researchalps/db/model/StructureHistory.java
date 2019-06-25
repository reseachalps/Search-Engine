package eu.researchalps.db.model;

import java.util.List;

/**
 * Historical modification of a research structure.
 */
public class StructureHistory {
    /**
     * Year of the event
     */
    private Integer date;
    /**
     * Type of event (not enumerated because we don't know the full extent of it)
     */
    private String type;
    /**
     * Structures Impacted
     */
    private List<String> structures;

    public StructureHistory(Integer date, String type, List<String> structures) {
        this.date = date;
        this.type = type;
        this.structures = structures;
    }

    public StructureHistory() {
    }

    public Integer getDate() {
        return date;
    }

    public String getType() {
        return type;
    }

    public List<String> getStructures() {
        return structures;
    }
}
