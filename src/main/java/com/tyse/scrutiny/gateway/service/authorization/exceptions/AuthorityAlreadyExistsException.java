package com.tyse.scrutiny.gateway.service.authorization.exceptions;

/**
 * Exception thrown when attempting to create an authority that already exists.
 */
public class AuthorityAlreadyExistsException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorityAlreadyExistsException(String code) {
        super("Authority with code '" + code + "' already exists");
    }
}
