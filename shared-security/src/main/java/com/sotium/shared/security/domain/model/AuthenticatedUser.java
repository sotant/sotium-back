package com.sotium.shared.security.domain.model;

import java.util.Set;

public record AuthenticatedUser(
    String sub,
    String email,
    Set<Role> roles
) {

    public boolean isAdmin() {
        return roles.contains(Role.ADMIN);
    }

    public boolean requiresTenant() {
        return roles.stream().anyMatch(Role::isTenantScoped);
    }

    public Set<String> authorities() {
        return Role.toAuthorities(roles);
    }
}
