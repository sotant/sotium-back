package com.sotium.shared.security.infrastructure.security.exceptions;

public class ForbiddenException extends RuntimeException {

    public ForbiddenException(final String message) {
        super(message);
    }
}
