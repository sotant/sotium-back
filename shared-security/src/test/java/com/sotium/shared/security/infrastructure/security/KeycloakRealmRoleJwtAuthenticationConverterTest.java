package com.sotium.shared.security.infrastructure.security;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class KeycloakRealmRoleJwtAuthenticationConverterTest {

    private final KeycloakRealmRoleJwtAuthenticationConverter converter = new KeycloakRealmRoleJwtAuthenticationConverter();

    @Test
    @DisplayName("jwtConverter_shouldBuildAuthenticationWithDetailsAndAuthorities_fromSupportedRealmRoles")
    void jwtConverter_shouldBuildAuthenticationWithDetailsAndAuthorities_fromSupportedRealmRoles() {
        final Jwt jwt = jwt(
            "sub-1",
            "owner@test.com",
            Map.of("realm_access", Map.of("roles", List.of("owner", "ADMIN")))
        );

        final JwtAuthenticationToken authentication = assertInstanceOf(JwtAuthenticationToken.class, converter.convert(jwt));
        final AuthenticatedUser details = assertInstanceOf(AuthenticatedUser.class, authentication.getDetails());
        final Set<String> authorities = authentication.getAuthorities().stream()
            .map(grantedAuthority -> grantedAuthority.getAuthority())
            .collect(Collectors.toSet());

        assertEquals("sub-1", authentication.getName());
        assertEquals("sub-1", details.sub());
        assertEquals("owner@test.com", details.email());
        assertEquals(Set.of("ROLE_OWNER", "ROLE_ADMIN"), authorities);
    }

    @Test
    @DisplayName("jwtConverter_shouldIgnoreUnknownRealmRolesWithoutThrowing")
    void jwtConverter_shouldIgnoreUnknownRealmRolesWithoutThrowing() {
        final Jwt jwt = jwt(
            "sub-1",
            "owner@test.com",
            Map.of("realm_access", Map.of("roles", List.of("foo", "bar")))
        );

        final JwtAuthenticationToken authentication = assertInstanceOf(JwtAuthenticationToken.class, converter.convert(jwt));

        assertTrue(authentication.getAuthorities().isEmpty());
    }

    @Test
    @DisplayName("jwtConverter_shouldReturnNoAuthorities_whenRealmAccessMissing")
    void jwtConverter_shouldReturnNoAuthorities_whenRealmAccessMissing() {
        final Jwt jwt = jwt("sub-1", "owner@test.com", Map.of());

        final JwtAuthenticationToken authentication = assertInstanceOf(JwtAuthenticationToken.class, converter.convert(jwt));

        assertTrue(authentication.getAuthorities().isEmpty());
        assertNotNull(authentication.getDetails());
    }

    @Test
    @DisplayName("jwtConverter_shouldFailFast_whenSubMissing")
    void jwtConverter_shouldFailFast_whenSubMissing() {
        final Jwt jwt = jwt(null, "owner@test.com", Map.of());

        final NullPointerException exception = assertThrows(NullPointerException.class, () -> converter.convert(jwt));

        assertEquals("JWT sub claim is required", exception.getMessage());
    }

    @Test
    @DisplayName("jwtConverter_shouldFailFast_whenEmailMissing")
    void jwtConverter_shouldFailFast_whenEmailMissing() {
        final Jwt jwt = jwt("sub-1", null, Map.of());

        final NullPointerException exception = assertThrows(NullPointerException.class, () -> converter.convert(jwt));

        assertEquals("JWT email claim is required", exception.getMessage());
    }

    private Jwt jwt(final String sub, final String email, final Map<String, Object> extraClaims) {
        final Map<String, Object> claims = new HashMap<>(extraClaims);
        if (sub != null) {
            claims.put("sub", sub);
        }
        if (email != null) {
            claims.put("email", email);
        }
        return new Jwt(
            "token-value",
            Instant.now(),
            Instant.now().plusSeconds(300),
            Map.of("alg", "none"),
            claims
        );
    }
}
