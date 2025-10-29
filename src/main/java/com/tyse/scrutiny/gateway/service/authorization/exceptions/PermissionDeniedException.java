package com.tyse.scrutiny.gateway.service.authorization.exceptions;

/**
 * Exception thrown when a user attempts an operation without sufficient permissions.
 */
public class PermissionDeniedException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public PermissionDeniedException(String message) {
        super(message);
    }
}
