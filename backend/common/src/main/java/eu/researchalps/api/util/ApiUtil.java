package eu.researchalps.api.util;

import eu.researchalps.api.exception.NotFoundException;
import org.springframework.data.repository.CrudRepository;

import java.io.Serializable;

/**
 *
 */
public class ApiUtil {
    public static <E> E getOrThrow(E e, String type, String ref) {
        if (e == null)
            throw new NotFoundException(type, ref);
        return e;
    }

    public static <E, F extends Serializable> E fetchOrThrow(CrudRepository<E, F> repository, String type, F ref) {
        E e = repository.findOne(ref);
        if (e == null)
            throw new NotFoundException(type, ref.toString());
        return e;
    }

    public static <E, F extends Serializable> void existsOrThrow(CrudRepository<E, F> repository, String type, F ref) {
        boolean test = repository.exists(ref);
        if (!test)
            throw new NotFoundException(type, ref.toString());
    }
}
