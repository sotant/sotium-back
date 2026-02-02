package com.sotium.shared.exception;

/**
 * Generic not-found exception to be reused across modules.
 */
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) { super(message); }
}
