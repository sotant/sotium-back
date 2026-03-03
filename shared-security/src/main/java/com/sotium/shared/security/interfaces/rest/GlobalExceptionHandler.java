package com.sotium.shared.security.interfaces.rest;

import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler({AccessDeniedException.class, ForbiddenException.class})
    public ResponseEntity<ProblemDetail> handleForbidden(final RuntimeException exception, final HttpServletRequest request) {
        log.warn("Forbidden request path={} message={}", request.getRequestURI(), exception.getMessage());
        return build(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ProblemDetail> handleUnauthorized(final AuthenticationException exception, final HttpServletRequest request) {
        log.warn("Unauthorized request path={} message={}", request.getRequestURI(), exception.getMessage());
        return build(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetail> handleBadRequest(final IllegalArgumentException exception, final HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    private ResponseEntity<ProblemDetail> build(final HttpStatus status, final String message, final String path) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, message);
        problemDetail.setTitle(status.getReasonPhrase());
        problemDetail.setInstance(java.net.URI.create(path));
        return ResponseEntity.status(status).body(problemDetail);
    }
}
