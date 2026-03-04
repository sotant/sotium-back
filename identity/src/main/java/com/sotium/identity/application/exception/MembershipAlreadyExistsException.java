package com.sotium.identity.application.exception;

public class MembershipAlreadyExistsException extends RuntimeException {

    public MembershipAlreadyExistsException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
