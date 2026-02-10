package com.sotium.shared.security.infrastructure.security;

import com.sotium.shared.security.domain.model.AuthenticatedUser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps Keycloak realm roles into Spring authorities while filtering unsupported roles.
 */
@Slf4j
public class KeycloakRealmRoleJwtAuthenticationConverter implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final Set<String> SUPPORTED_REALM_ROLES = Set.of("ADMIN", "OWNER", "TEACHER", "STUDENT");

    @Override
    public AbstractAuthenticationToken convert(final Jwt jwt) {
        final Set<String> realmRoles = extractRealmRoles(jwt);
        final Set<String> authoritiesAsText = realmRoles.stream()
            .map(role -> "ROLE_" + role)
            .collect(Collectors.toSet());
        final Collection<GrantedAuthority> authorities = authoritiesAsText.stream()
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toSet());

        if (authoritiesAsText.isEmpty()) {
            log.debug("No supported realm roles found for subject={}", jwt.getSubject());
        }

        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(
            JwtClaimsExtractor.sub(jwt),
            JwtClaimsExtractor.email(jwt),
            realmRoles,
            authoritiesAsText
        );

        final JwtAuthenticationToken authentication = new JwtAuthenticationToken(jwt, authorities, authenticatedUser.sub());
        authentication.setDetails(authenticatedUser);
        return authentication;
    }

    private Set<String> extractRealmRoles(final Jwt jwt) {
        final Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        if (realmAccess == null || realmAccess.isEmpty()) {
            return Set.of();
        }

        final Object roles = realmAccess.get("roles");
        if (!(roles instanceof List<?> roleList)) {
            return Set.of();
        }

        return roleList.stream()
            .filter(String.class::isInstance)
            .map(String.class::cast)
            .map(role -> role.toUpperCase(Locale.ROOT))
            .filter(SUPPORTED_REALM_ROLES::contains)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Set::copyOf));
    }
}
