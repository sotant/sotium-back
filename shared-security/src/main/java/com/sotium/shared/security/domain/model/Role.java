package com.sotium.shared.security.domain.model;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.stream.Collectors;

public enum Role {
    ADMIN(false),
    OWNER(true),
    TEACHER(true),
    STUDENT(true);

    private final boolean tenantScoped;

    Role(final boolean tenantScoped) {
        this.tenantScoped = tenantScoped;
    }

    public boolean isTenantScoped() {
        return tenantScoped;
    }

    public String asAuthority() {
        return "ROLE_" + name();
    }

    public static Set<String> toAuthorities(final Set<Role> roles) {
        return roles.stream()
            .map(Role::asAuthority)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Set::copyOf));
    }

    public static Set<Role> fromRealmRoles(final Set<String> realmRoles) {
        return realmRoles.stream()
            .map(role -> role.toUpperCase(Locale.ROOT))
            .map(Role::safeValueOf)
            .flatMap(java.util.Optional::stream)
            .collect(Collectors.collectingAndThen(Collectors.toCollection(LinkedHashSet::new), Set::copyOf));
    }

    private static java.util.Optional<Role> safeValueOf(final String roleName) {
        try {
            return java.util.Optional.of(Role.valueOf(roleName));
        } catch (IllegalArgumentException exception) {
            return java.util.Optional.empty();
        }
    }
}
