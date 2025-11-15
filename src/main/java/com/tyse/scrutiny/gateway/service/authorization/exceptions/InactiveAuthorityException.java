package com.tyse.scrutiny.gateway.service.authorization.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.ErrorResponseException;
import tech.jhipster.web.rest.errors.ProblemDetailWithCause.ProblemDetailWithCauseBuilder;

/**
 * Exception thrown when attempting to modify an inactive authority.
 */
@SuppressWarnings("java:S110") // Inheritance tree of classes should not be too deep
public class InactiveAuthorityException extends ErrorResponseException {

    private static final long serialVersionUID = 1L;

    private final String authorityCode;

    public InactiveAuthorityException(String authorityCode) {
        super(
            HttpStatus.BAD_REQUEST,
            ProblemDetailWithCauseBuilder.instance()
                .withStatus(HttpStatus.BAD_REQUEST.value())
                .withTitle("Cannot modify inactive authority")
                .withDetail("Cannot modify inactive authority: " + authorityCode)
                .withProperty("message", "error.authority.inactive")
                .withProperty("authorityCode", authorityCode)
                .build(),
            null
        );
        this.authorityCode = authorityCode;
    }

    public String getAuthorityCode() {
        return authorityCode;
    }
}
