package com.tyse.scrutiny.gateway.service.authorization.exceptions;

/**
 * Exception thrown when an authority cannot be found.
 */
public class AuthorityNotFoundException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public AuthorityNotFoundException(Long id) {
        super("Authority not found with id: " + id);
    }

    public AuthorityNotFoundException(String code) {
        super("Authority not found with code: " + code);
    }
}
