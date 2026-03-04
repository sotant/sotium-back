package com.sotium.onboarding.application.exception;

public class RegisterAcademyConflictException extends RuntimeException {

    public RegisterAcademyConflictException(final String message, final Throwable cause) {
        super(message, cause);
    }
}
