package eu.researchalps.search.repository.impl;

import eu.researchalps.search.model.request.MultiValueSearchFilter;

/**
 * TODO: catch this excepttion and transform to MvcException
 */
public class InvalidSearchFilterException extends RuntimeException {

    public static class Body {
        public MultiValueSearchFilter filter;
        public String reason;
        public String attribute;

        public Body(MultiValueSearchFilter filter, String reason) {
            this.filter = filter;
            this.reason = reason;
        }

        public Body(MultiValueSearchFilter filter, String attribute, String reason) {
            this.filter = filter;
            this.reason = reason;
            this.attribute = attribute;
        }
    }

    private final Body body;

    public InvalidSearchFilterException(MultiValueSearchFilter filter, String reason) {
        this.body = new Body(filter, reason);
    }

    public InvalidSearchFilterException(MultiValueSearchFilter filter, String attribute, String reason) {
        this.body = new Body(filter, attribute, reason);
    }

    public Body getBody() {
        return body;
    }
}
