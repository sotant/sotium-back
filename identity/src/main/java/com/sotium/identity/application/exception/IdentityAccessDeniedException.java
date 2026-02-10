package com.sotium.identity.application.exception;

public class IdentityAccessDeniedException extends RuntimeException {

    public IdentityAccessDeniedException(final String message) {
        super(message);
    }
}
