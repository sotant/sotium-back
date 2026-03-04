package com.sotium.identity.application.exception;

public class IdentityUserEmailConflictException extends RuntimeException {

    public IdentityUserEmailConflictException(final String message) {
        super(message);
    }
}
