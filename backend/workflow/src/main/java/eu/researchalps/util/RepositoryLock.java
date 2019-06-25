package eu.researchalps.util;

import eu.researchalps.api.exception.NotFoundException;
import com.google.common.util.concurrent.Striped;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.function.Consumer;
import java.util.function.Function;

/**
 * Create a lock-system over resources of a crud repository. This guarantees the concurrent access to a resource to
 * modify it in local isolation.
 *
 * Isolation is guaranteed per resource (update methods) or as a global lock when a whole operation over the repository
 * (updateGlobally methods).
 *
 * The locks are provided by a striped lock which guarantees that equivalent keys will have the same lock and therefore
 * be mutually exclusive.
 * http://docs.guava-libraries.googlecode.com/git/javadoc/com/google/common/util/concurrent/Striped.html
 *
 * The global lock is guaranteed by a standard re-entrant read-write lock. Key-based transactions are managed by the
 * "read" lock, global updates are managed by the "write" lock.
 *
 * Standard usage is as such:
 *
 * RepositoryLock<Model, String, Repository> repositoryLock = RepositoryLock.get(repository);
 * Model asSaved = repositoryLock.update(id, tx -> {
 *      // Isolation scope!
 *      Model m = tx.getData();
 *      // do stuff with it...
 *      // ...
 *
 *      tx.saveNow();
 * }).getSaved();
 *
 * Each isolation scope is provided with a transaction object (tx). This provides the object, and allows save methods.
 * An isolation scope can return a value which will be given as a result of the current method.
 *
 * Details about concurrency issues are provided here:
 * https://data-publica.atlassian.net/wiki/pages/viewpage.action?pageId=36470860
 *
 * @param <MODEL> The model of the resources
 * @param <KEY> The primary key type of the resources
 * @param <REP> The repository class
 */
public class RepositoryLock<MODEL, KEY extends Serializable, REP extends CrudRepository<MODEL, KEY>> {
    private REP repository;

    private Striped<Lock> lock = Striped.lock(Runtime.getRuntime().availableProcessors() * 4);

    private ReadWriteLock globalLock = new ReentrantReadWriteLock();

    private RepositoryLock(REP repository) {
        this.repository = repository;
    }

    /**
     * Creates an isolation scope over the given key and returns a value given by the transaction.
     *
     * The isolation scope is mutually exclusive with updates of the same key and updateGlobally.
     *
     * @param key The requested resource id
     * @param transaction The isolation scope
     * @param <E> The return value type
     * @return A transaction result with the return value embedded in it along with the input data
     */
    public <E> TxResult<E> updateAndReturn(KEY key, Function<Tx, E> transaction) {
        Lock repositoryAccessLock = globalLock.readLock();
        repositoryAccessLock.lock();
        try {
            return perKeyTx(key, transaction);
        } finally {
            repositoryAccessLock.unlock();
        }
    }

    /**
     * Same as #updateAndReturn by returning Object null.
     *
     * @param key The requested resource id
     * @param transaction The isolation scope
     * @return A transaction result with the input data
     */
    public TxResult<Object> update(KEY key, Consumer<Tx> transaction) {
        return updateAndReturn(key, it -> {
            transaction.accept(it);
            return null;
        });
    }

    /**
     * Creates an isolation scope over the repository and returns a value given by the transaction.
     *
     * The isolation scope is mutually exclusive with any other updates performed by this repository.
     *
     * @param update The isolation scope which provide the repository as argument
     * @return The value given by the lambda
     */
    public <E> E updateGloballyAndReturn(Function<REP, E> update) {
        Lock repositoryAccessLock = globalLock.readLock();
        repositoryAccessLock.lock();
        try {
            return update.apply(repository);
        } finally {
            repositoryAccessLock.unlock();
        }
    }

    /**
     * Same as #updateGloballyAndReturn without return value
     *
     * @param update The isolation scope which provide the repository as argument
     */
    public void updateGlobally(Consumer<REP> update) {
        updateGloballyAndReturn(it -> {
            update.accept(it);
            return null;
        });
    }

    /**
     * Utility method: read a resource from the repository. This method is read-only and no transaction must be done.
     *
     * @param key The resource id
     * @return The given document
     */
    public MODEL read(KEY key) {
        return repository.findOne(key);
    }

    /**
     * Get the repository instance. This does not guarantee anything about the concurrency. That is why it must be used
     * only as a read-only method.
     *
     * @return The repository
     */
    public REP readOnly() {
        return repository;
    }

    /**
     * Performs the transaction of the update. This does not manage repository locks which are done in update methods.
     *
     * If any exception is thrown by the transaction, then, no save is done.
     *
     * @param key The requested resource id
     * @param transaction The isolation scope
     * @param <E> The return value type
     * @return A transaction result with the return value embedded in it along with the input data
     */
    private <E> TxResult<E> perKeyTx(KEY key, Function<Tx, E> transaction) {
        Lock l = lock.get(key);
        l.lock();
        try {
            // Make sure that the whole transaction is made in an "acid" manner
            MODEL w = repository.findOne(key);
            Tx tx = new Tx(w, key);
            E result = transaction.apply(tx);
            if (tx.output != null && !tx.outputSaved)
                tx.output = repository.save(tx.output);

            return new TxResult<>(tx, result);
        } finally {
            l.unlock();
        }
    }

    private static final Map<Object, RepositoryLock> instances = new HashMap<>();

    /**
     * Get the unique instance of repository lock for the given repository.
     *
     * If rep1.equals(rep2), then get(rep1).equals(get(rep2)).
     *
     * @param repository The repository instance
     * @param <MODEL> The model of the resources
     * @param <KEY> The primary key type of the resources
     * @param <REP> The repository class
     * @return The repository lock instance
     */
    public static <MODEL, KEY extends Serializable, REP extends CrudRepository<MODEL, KEY>>
            RepositoryLock<MODEL, KEY, REP> get(REP repository) {
        RepositoryLock repositoryLock = instances.get(repository);
        if(repositoryLock == null) {
            RepositoryLock<MODEL, KEY, REP> instance = new RepositoryLock<>(repository);
            instances.put(repository, instance);
            return instance;
        }
        //noinspection unchecked
        return repositoryLock;
    }

    /**
     * The transaction object
     */
    public class Tx {
        private MODEL input;

        private MODEL output;

        private KEY key;

        private boolean outputSaved = false;

        private Tx(MODEL input, KEY key) {
            this.input = input;
            this.key = key;
        }

        /**
         * Get the asked resource
         *
         * @return The resource object
         */
        public MODEL get() {
            return input;
        }

        /**
         * Get the asked resource and throw a NotFoundException if the result is null.
         *
         * @return The non-null resource object
         * @throws NotFoundException if the resource is not found
         */
        public MODEL getNotNull() {
            if (input == null)
                throw new NotFoundException(repository.toString()+"/get", key.toString());
            return input;
        }

        /**
         * Recommended method to use: save the input object at the end of the transaction.
         *
         * @throws NotFoundException if input was empty
         */
        public void saveDeferred() {
            saveDeferred(getNotNull());
        }

        /**
         * Recommended method to use: save the given object at the end of the transaction.
         *
         * @param output The given resource
         * @throws IllegalArgumentException if the resource is null
         */
        public void saveDeferred(MODEL output) {
            if (output == null) {
                throw new IllegalArgumentException("Transaction output is null");
            }
            this.output = output;
            outputSaved = false;
        }

        /**
         * Save the object right now (call repository.save(object)).
         *
         * @return The saved object
         */
        public MODEL saveNow() {
            return saveNow(getNotNull());
        }

        /**
         * Save the object right now (call repository.save(object)).
         *
         * @param output The object to save
         * @return The saved object
         * @throws IllegalArgumentException if the resource is null
         */
        public MODEL saveNow(MODEL output) {
            if (output == null) {
                throw new IllegalArgumentException("Transaction output is null");
            }
            this.output = output;
            this.output = repository.save(output);
            outputSaved = true;
            return this.output;
        }

        public void deleteNow() {
            repository.delete(getNotNull());
            outputSaved = true;
            this.output = null;
        }
    }

    /**
     * The result of a transaction done by a update method.
     *
     * @param <E> The result type
     */
    public class TxResult<E> {
        private MODEL input;
        private MODEL saved;
        private E result;

        private TxResult(Tx tx, E result) {
            input = tx.input;
            saved = tx.output;
            this.result = result;
        }

        /**
         * Get the input as given by the repository.
         *
         * @return The input
         */
        public MODEL getInput() {
            return input;
        }

        /**
         * Get the most fresh instance of the data. If it has been saved, then return the saved one, else the input.
         *
         * @return The most fresh instance of the resource
         */
        public MODEL getData() {
            return saved == null ? input : saved;
        }

        /**
         * Get the saved instance. null if the instance was not saved.
         *
         * @return The saved instance.
         */
        public MODEL getSaved() {
            return saved;
        }

        /**
         * Return the transaction result
         *
         * @return The result
         */
        public E getResult() {
            return result;
        }
    }
}
