package eu.researchalps.api.exception;

import org.springframework.http.HttpStatus;

/**
 *
 */
public interface MvcException<E> {
    public HttpStatus getStatus();

    public E getBody();
}
