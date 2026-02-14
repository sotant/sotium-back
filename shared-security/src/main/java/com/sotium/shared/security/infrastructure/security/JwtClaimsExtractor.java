package com.sotium.shared.security.infrastructure.security;

import org.springframework.security.oauth2.jwt.Jwt;

import java.util.Objects;

public final class JwtClaimsExtractor {

    private JwtClaimsExtractor() {
    }

    public static String sub(final Jwt jwt) {
        return Objects.requireNonNull(jwt.getSubject(), "JWT sub claim is required");
    }

    public static String email(final Jwt jwt) {
        return Objects.requireNonNull(jwt.getClaimAsString("email"), "JWT email claim is required");
    }
}
