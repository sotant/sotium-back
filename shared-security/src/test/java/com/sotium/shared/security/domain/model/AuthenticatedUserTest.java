package com.sotium.shared.security.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AuthenticatedUserTest {

    @Test
    @DisplayName("authenticatedUser_isAdmin_shouldReturnTrueWhenRoleAdminPresent")
    void authenticatedUser_isAdmin_shouldReturnTrueWhenRoleAdminPresent() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("sub-1", "admin@test.com", Set.of(Role.ADMIN));

        assertTrue(authenticatedUser.isAdmin());
    }

    @Test
    @DisplayName("authenticatedUser_requiresTenant_shouldReturnTrueForTenantScopedRoles")
    void authenticatedUser_requiresTenant_shouldReturnTrueForTenantScopedRoles() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER));

        assertTrue(authenticatedUser.requiresTenant());
    }

    @Test
    @DisplayName("authenticatedUser_authorities_shouldDeriveFromRoles")
    void authenticatedUser_authorities_shouldDeriveFromRoles() {
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("sub-1", "user@test.com", Set.of(Role.ADMIN, Role.STUDENT));

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_STUDENT"), authenticatedUser.authorities());
    }
}
