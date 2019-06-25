package eu.researchalps.workflow.full;

import com.datapublica.companies.workflow.service.QueueService;
import eu.researchalps.db.model.Structure;
import eu.researchalps.db.model.full.FullStructure;
import eu.researchalps.db.model.full.FullStructureField;
import eu.researchalps.db.repository.FullStructureRepository;
import eu.researchalps.db.repository.StructureRepository;
import eu.researchalps.workflow.search.IndexStructureProcess;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.Striped;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.Lock;

/**
 *
 */
@Service
public class FullStructureService {
    private static final Logger log = LoggerFactory.getLogger(FullStructureService.class);

    private Striped<Lock> lock = Striped.lock(64);

    @Autowired(required = false)
    private List<FullStructureProvider> providers = Lists.newArrayList();

    @Autowired
    private FullStructureRepository repository;

    @Autowired
    private FullStructureCorrecter fullStructureCorrecter;

    @Autowired
    private StructureRepository structureRepository;
    @Autowired
    private QueueService service;

    private Map<FullStructureField, FullStructureProvider> providerIdx = new EnumMap<>(FullStructureField.class);
    private Multimap<FullStructureField, FullStructureField> transitiveDependencies = HashMultimap.create();
    private Multimap<FullStructureField, FullStructureField> directDependencies = HashMultimap.create();

    @PostConstruct
    protected void init() {
        // Compute direct dependencies graph & provider index
        for (FullStructureProvider<?> provider : providers) {
            providerIdx.put(provider.getField(), provider);
            log.info("Registering provider {} for field {}", provider.getClass().getName(), provider.getField());
            for (FullStructureField field : provider.getDependencies()) {
                directDependencies.put(field, provider.getField());
            }
        }

        computeTransitiveDependencies();
    }

    protected void computeTransitiveDependencies() {
        // Compute the transitive dependencies
        for (FullStructureField field : directDependencies.keySet()) {
            Set<FullStructureField> deps = Sets.newHashSet();
            fillDependencies(field, directDependencies, deps);
            transitiveDependencies.putAll(field, deps);
        }
    }

    private static void fillDependencies(FullStructureField field, Multimap<FullStructureField, FullStructureField> dep,
                                         Set<FullStructureField> result) {
        if (!dep.containsKey(field)) {
            return;
        }
        for (FullStructureField depField : dep.get(field)) {
            result.add(depField);
            fillDependencies(depField, dep, result);
        }
    }

    /**
     * Create a transaction on an identifier. When you are inside the context of a TX you are sure that you are alone
     * working on this structure. Also, the save will throw a full structure updated message.
     * <p/>
     * <pre>
     *    try(FullStructureTransaction tx = service.tx("FR-123456789", false)) {
     *        tx.setField(...);
     *        tx.save();
     *    }
     * </pre>
     *
     * @param id The structure identifier
     * @param failOnAbsent If true,
     * @return The TX object
     */
    public FullStructureTransaction tx(String id, boolean failOnAbsent) {
        Lock l = lock.get(id);
        l.lock();
        try {
            FullStructure f = repository.findOne(id);
            if (f != null) {
                return new FullStructureTransaction(this, f, l, false);
            }
            Structure c = structureRepository.findOne(id);
            if (c == null) {
                if (failOnAbsent) {
                    throw new IllegalArgumentException("Unknown structure " + id);
                } else {
                    l.unlock();
                    return null;
                }
            }
            log.debug("Creating full structure " + id);

            // Create the new full structure
            FullStructure fc = new FullStructure(id);
            fc.setStructure(c);

            Set<FullStructureField> changedFields = cleanDirtyFields(fc, Sets.newHashSet(FullStructureField.values()));
            FullStructureTransaction fct = new FullStructureTransaction(this, fc, l, true);
            fct.getModifiedFields().addAll(changedFields);
            return fct;
        } catch (Exception e) {
            l.unlock();
            throw e;
        }
    }

    /**
     * Utility method that allows a user to refresh a field easily
     *
     * @param id    The identifier
     * @param field The field to update
     */
    public void refresh(String id, FullStructureField field) {
        try (FullStructureTransaction tx = tx(id, true)) {
            tx.refresh(field);
            tx.save(false, false);
        }
    }

    /**
     * Utility method that allows a user to refresh a field easily
     * Does not throw IAE if the structure doesnt exist
     *
     * @param id    The identifier
     * @param field The field to update
     */
    public void refreshIfExists(String id, FullStructureField field) {
        try (FullStructureTransaction tx = tx(id, false)) {
            if (tx == null) {
                return;
            }
            tx.refresh(field);
            tx.save(false, false);
        }
    }

    /**
     * Shortcut of #getAndUpdateField with create = true
     *
     * @param id    The identifier
     * @param field The field to update
     * @param data  The data to put
     * @param <E>   The data type
     */
    public <E> void getAndUpdateField(String id, FullStructureField field, E data) {
        try (FullStructureTransaction tx = tx(id, true)) {
            tx.setField(field, data);
            tx.save(false, false);
        }
    }

    /**
     * Utility method to ensure that the id is created in the base.
     *
     * @param id The The structure identifier
     */
    public void ensureCreated(String id) {
        try (FullStructureTransaction tx = tx(id, true)) {
            tx.save(false, false);
        }
    }

    /**
     * Utility method to ensure that the id is created in the base.
     *
     * @param id The The structure identifier
     */
    public void delayedRefresh(String id, FullStructureField... fields) {
        Lock l = lock.get(id);
        l.lock();
        try {
            repository.addDelayedFieldToRefresh(id, fields);
        } finally {
            l.unlock();
        }
    }

    /**
     * Utility method to ensure that the id is created in the base.
     *
     * @param id The The structure identifier
     */
    public void notifyIndexed(String id) {
        Lock l = lock.get(id);
        l.lock();
        try {
            repository.notifyIndexed(id);
        } finally {
            l.unlock();
        }
    }

    protected void save(FullStructureTransaction tx, boolean forcePropagation, boolean forIndex) {
        Structure innerStructure = tx.getData().getStructure();
        Set<FullStructureField> modifiedFields = tx.getModifiedFields();
        if (innerStructure == null) {
            // If the structure has been delete, then the full structure itself must be deleted and notification propagated
            log.debug("[{}] Removing full structure", tx.getData().getId());
            repository.delete(tx.getData().getId());
            forcePropagation = true;
        } else {
            boolean corrected = fullStructureCorrecter.correctStructure(tx.getData());
            if (modifiedFields.isEmpty() && !corrected) {
                if (forIndex) {
                    repository.notifyIndexed(tx.getData().getId());
                    log.debug("[{}] Nothing to do... only saving index status", tx.getData().getId());
                }
                // Nothing to actually do...
                return;
            }
            tx.getData().setIndexed(forIndex);
            repository.save(tx.getData());
            log.debug("[{}] Saving... fields that have changed {}", tx.getData().getId(), modifiedFields);
        }
        if (forcePropagation) {
            // Notify the index that the structure needs to be updated
            service.push(tx.getData().getId(), IndexStructureProcess.QUEUE);
        }
    }

    /**
     * Set the field from a full structure and update all the fields based on their providers.
     *
     * @param Structure The full structure object
     * @param field     The field to update
     * @param data      The data to put
     * @param <E>       The data type
     * @return The list of updated fields (the given field is included)
     */
    protected <E> Set<FullStructureField> setField(FullStructure Structure, FullStructureField field, E data) {
        field.setter().accept(Structure, data);
        Set<FullStructureField> changedFields = cleanDirtyFields(Structure, Sets.newHashSet(directDependencies.get(field)));
        changedFields.add(field);
        return changedFields;
    }

    /**
     * Check of clean fields that are dirty. This will call for providers to create their respective entries.
     * <p/>
     * This must guarantee the following restrictions:
     * <li>A provider is called if it agreed on the content (via #canProvide)</li>
     * <li>A provider is called AFTER all dependent dirty fields have been resolved</li>
     * <li>A field is marked dirty if a dependent provider has provided a new value that was not equal (with #equals)
     * to the previous value</li>
     * <li>A field is reset to null if the provider cannot provide, and must propagate its new null value (if necessary)</li>
     *
     * @param structure   The full structure object
     * @param dirtyFields The dirty fields to check or clean
     * @return The list of affected fields
     */
    protected Set<FullStructureField> cleanDirtyFields(FullStructure structure, Set<FullStructureField> dirtyFields) {
        Set<FullStructureField> changedFields = Sets.newHashSet();
        if (dirtyFields.isEmpty())
            return changedFields;

        Multimap<FullStructureField, FullStructureField> reverseDependencies = HashMultimap.create();

        // While we still have fields to clean
        do {
            // Compute the reverse dependencies graph
            reverseDependencies.clear();
            for (FullStructureField dirtyField : dirtyFields) {
                transitiveDependencies.get(dirtyField).stream()
                        .filter(dirtyFields::contains)
                        .forEach(dirtyDep -> reverseDependencies.put(dirtyDep, dirtyField));
            }

            // Check for any field that has no dependency
            FullStructureField dirtyField = dirtyFields.stream().filter(it -> reverseDependencies.get(it).isEmpty()).findAny().get();

            // Get the provider
            FullStructureProvider provider = providerIdx.get(dirtyField);
            if (provider == null) {
                throw new IllegalStateException("No provider were found for " + dirtyField);
            }

            // Try to set the field
            Object old = dirtyField.getter().apply(structure);

            log.trace("[{}] Calling provider for field {}", structure.getId(), dirtyField);
            Object newValue = null;
            // Update if necessary
            if (provider.canProvide(structure)) {
                newValue = provider.computeField(structure);
            }
            // If old and new are considered equal, there no need to propagate the changes
            if (old == null && newValue != null || old != null && !old.equals(newValue)) {
                log.trace("[{}] Field {} has changed, propagating to dependencies", structure.getId(), dirtyField);
                dirtyField.setter().accept(structure, newValue);
                changedFields.add(dirtyField);

                // Add the newly dirty fields
                dirtyFields.addAll(directDependencies.get(dirtyField));
            } else if (newValue == null) {
                log.trace("[{}] Field {} remains null", structure.getId(), dirtyField);
            }

            if (dirtyField == FullStructureField.STRUCTURE && newValue == null) {
                // Structure has been removed!!!
                // Stop right there or there will be consequences
                return changedFields;
            }

            // Now this is clean
            dirtyFields.remove(dirtyField);
        } while (!dirtyFields.isEmpty());
        return changedFields;
    }

    protected Map<FullStructureField, FullStructureProvider> getProviderIdx() {
        return providerIdx;
    }

    protected Multimap<FullStructureField, FullStructureField> getTransitiveDependencies() {
        return transitiveDependencies;
    }

    protected Multimap<FullStructureField, FullStructureField> getDirectDependencies() {
        return directDependencies;
    }

    public FullStructureRepository getRepository() {
        return repository;
    }
}
