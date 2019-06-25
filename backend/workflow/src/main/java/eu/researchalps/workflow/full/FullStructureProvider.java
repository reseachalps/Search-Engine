package eu.researchalps.workflow.full;

import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;

import java.util.Set;

/**
 * Interface of a full company provider service
 *
 * Each provider is able to produce one field
 */
public interface FullStructureProvider<E> {
    /**
     * Can the service provide something? It is a good idea to check for nullable entries or similar.
     *
     * @param company The current state of the full company
     * @return true if computeField can be called without restriction
     */
    default public boolean canProvide(FullStructure company) {
        return true;
    }
    /**
     * Compute the field from the current state of the company.
     *
     * The state of the company must be up to date wrt the dependencies
     *
     * @param company The current state of the full company
     * @return The new value of the field
     */
    public E computeField(FullStructure company);

    /**
     * Which field this service can provide?
     *
     * @return The field
     */
    public FullStructureField getField();

    /**
     * The list of fields that this provider depends on.
     *
     * @return The list
     */
    public Set<FullStructureField> getDependencies();
}
