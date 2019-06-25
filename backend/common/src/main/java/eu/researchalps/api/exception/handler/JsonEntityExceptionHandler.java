package eu.researchalps.api.exception.handler;

import eu.researchalps.api.exception.MvcException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;

/**
 *
 */
public class JsonEntityExceptionHandler extends ResponseEntityExceptionHandler {
	
	/**
	 * 
	 */
	Logger logger = LoggerFactory.getLogger(JsonEntityExceptionHandler.class);
	

    protected <E> ResponseEntity<Object> handleMvcException(MvcException<E> ex, WebRequest request) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        logger.error("error in Api", ex);
        ExceptionMessage<E> message = createMessage((Exception) ex, request, ex.getBody());
        return super.handleExceptionInternal((Exception) ex, message, headers, ex.getStatus(), request);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object b, HttpHeaders headers, HttpStatus status, WebRequest request) {
        headers.setContentType(MediaType.APPLICATION_JSON);
        Object body = createMessage(ex, request, ReadableStackTrace.getTrace(ex));

        return super.handleExceptionInternal(ex, body, headers, status, request);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception e, HttpStatus status, WebRequest request) {
        return handleExceptionInternal(e, status, request, false);
    }

    protected ResponseEntity<Object> handleExceptionInternal(Exception e, HttpStatus status, WebRequest request, boolean emptyBody) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        List<ReadableStackTrace> body = emptyBody ? null : ReadableStackTrace.getTrace(e);
        ExceptionMessage<Object> message = createMessage(e, request, body);
        logger.error("error in Api", e);
        return super.handleExceptionInternal(e, message, headers, status, request);
    }

    protected <E> ExceptionMessage<E> createMessage(Exception ex, WebRequest request, E body) {
        ExceptionMessage<E> message = new ExceptionMessage<>(ex, request);
        message.details = body;
        return message;
    }
}
