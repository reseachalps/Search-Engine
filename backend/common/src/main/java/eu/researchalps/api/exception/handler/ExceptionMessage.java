package eu.researchalps.api.exception.handler;

import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;

import javax.servlet.http.HttpServletRequest;
import java.security.Principal;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class ExceptionMessage<E> {
    public final String message;
    public Date timestamp = new Date();
    public String error;
    public E details;
    public WebRequestDTO request;

    protected ExceptionMessage(Exception e, WebRequestDTO dto) {
        this.error = e.getClass().getSimpleName();
        this.message = e.getMessage();
        this.request = dto;
    }

    public ExceptionMessage(Exception e, WebRequest request) {
        this(e, new WebRequestDTO(request));
    }

    public ExceptionMessage(Exception e, HttpServletRequest request) {
        this(e, new WebRequestDTO(request));
    }


    public static class WebRequestDTO {
        public final Map<String, String[]> parameters;
        public final String user;
        public final String path;

        public WebRequestDTO(WebRequest request) {
            path = request instanceof ServletWebRequest ? ((ServletWebRequest) request).getRequest().getRequestURI() : null;
            parameters = request.getParameterMap();
            Principal user = request.getUserPrincipal();
            this.user = user != null ? user.getName() : null;
        }

        public WebRequestDTO(HttpServletRequest request) {
            Principal user = request.getUserPrincipal();
            this.user = user != null ? user.getName() : null;
            parameters = request.getParameterMap();
            path = request.getRequestURI();
        }
    }
}
