package eu.researchalps.api.exception;

import org.springframework.http.HttpStatus;

public class NotFoundException extends RuntimeException implements MvcException<NotFoundException.Body> {
    public static class Body {
        public String type;
        public String ref;

        public Body(String type, String ref) {
            this.type = type;
            this.ref = ref;
        }
    }

    private Body body;

    public NotFoundException(String type, String ref) {
        super("Cannot find resource of type " + type + " for reference " + ref);
        body = new Body(type, ref);
    }

    @Override
    public HttpStatus getStatus() {
        return HttpStatus.NOT_FOUND;
    }

    @Override
    public Body getBody() {
        return body;
    }
}
