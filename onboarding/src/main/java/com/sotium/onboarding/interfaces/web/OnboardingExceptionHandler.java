package com.sotium.onboarding.interfaces.web;

import com.sotium.onboarding.application.exception.RegisterAcademyConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice(basePackageClasses = OnboardingAcademyController.class)
public class OnboardingExceptionHandler {

    @ExceptionHandler({MethodArgumentNotValidException.class, IllegalArgumentException.class, NullPointerException.class})
    public ResponseEntity<ProblemDetail> handleValidation(
        final Exception exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.BAD_REQUEST, "VALIDATION_ERROR", "Invalid onboarding academy request", request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(
        final AuthenticationException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.UNAUTHORIZED, "UNAUTHORIZED", "Authentication is required", request.getRequestURI());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ProblemDetail> handleForbidden(
        final AccessDeniedException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN", "Access denied", request.getRequestURI());
    }

    @ExceptionHandler(RegisterAcademyConflictException.class)
    public ResponseEntity<ProblemDetail> handleConflict(
        final RegisterAcademyConflictException exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.CONFLICT, "CONFLICT", "Onboarding request conflicts with existing data", request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleInternal(
        final Exception exception,
        final HttpServletRequest request
    ) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", "Unexpected error while registering academy", request.getRequestURI());
    }

    private ResponseEntity<ProblemDetail> build(
        final HttpStatus status,
        final String code,
        final String detail,
        final String path
    ) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, detail);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setProperty("code", code);
        problemDetail.setInstance(URI.create(path));
        return ResponseEntity.status(status).body(problemDetail);
    }
}
