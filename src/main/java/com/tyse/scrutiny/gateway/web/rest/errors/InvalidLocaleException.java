package com.tyse.scrutiny.gateway.web.rest.errors;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

/**
 * Exception thrown when an invalid locale/language key is provided.
 */
@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class InvalidLocaleException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    public InvalidLocaleException() {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withType(ErrorConstants.DEFAULT_TYPE)
                .withTitle("Invalid language key")
                .withDetail("Only 'es' and 'en' are supported")
                .withProperty("message", "error.invalidlangkey")
                .build(),
            null
        );
    }
}
