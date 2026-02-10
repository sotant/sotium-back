package com.sotium.shared.security.domain.model;

import java.util.Set;

public record AuthenticatedUser(
    String sub,
    String email,
    Set<String> realmRoles,
    Set<String> authorities
) {
}
