package com.tyse.scrutiny.gateway.service.authorization.exceptions;

/**
 * Exception thrown when an invalid authority assignment is attempted.
 */
public class InvalidAuthorityAssignmentException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public InvalidAuthorityAssignmentException(String message) {
        super(message);
    }
}
