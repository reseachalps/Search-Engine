package eu.researchalps.db.model.full;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Relationships toward other structures.
 * One GraphElement by structure related to the FullStructure.
 * A relationship is created when other structures have common projects, publications...
 */
public class GraphElement {

    /**
     * target Strcuture of the relationship
     */
    private LightStructure structure;
    /**
     * scores of this relationship.
     */
    private Map<GraphElementType, Integer> details = new HashMap<>();

    public GraphElement() {
    }

    public int getWeight() {
        return details.values().stream().mapToInt(Integer::intValue).sum();
    }

    public LightStructure getStructure() {
        return structure;
    }

    public void addElement(GraphElementType type) {
        Integer value = details.getOrDefault(type, 0) + 1;
        details.put(type, value);
    }

    public Map<GraphElementType, Integer> getDetails() {
        return details;
    }

    public void setStructure(LightStructure structure) {
        this.structure = structure;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GraphElement that = (GraphElement) o;
        return Objects.equals(structure, that.structure) &&
                Objects.equals(details, that.details);
    }

    @Override
    public int hashCode() {
        return Objects.hash(structure, details);
    }
}
