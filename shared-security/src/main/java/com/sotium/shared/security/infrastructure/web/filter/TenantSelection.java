package com.sotium.shared.security.infrastructure.web.filter;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

@Component
public class TenantSelection {

    private static final String TENANT_HEADER = "X-Academy-Id";
    private static final String TENANT_COOKIE = "academy_id";

    public Optional<UUID> resolveActiveAcademyId(final HttpServletRequest request) {
        final String headerValue = request.getHeader(TENANT_HEADER);
        if (headerValue != null && !headerValue.isBlank()) {
            return Optional.of(UUID.fromString(headerValue));
        }

        final Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return Optional.empty();
        }

        return Arrays.stream(cookies)
            .filter(cookie -> TENANT_COOKIE.equals(cookie.getName()))
            .map(Cookie::getValue)
            .filter(value -> value != null && !value.isBlank())
            .map(UUID::fromString)
            .findFirst();
    }
}
