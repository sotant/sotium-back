package com.sotium.shared.exception;

/**
 * Generic conflict exception (HTTP 409) to be reused across modules.
 */
public class ConflictException extends RuntimeException {
    public ConflictException(String message) { super(message); }
}
