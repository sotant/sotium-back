package com.sotium.shared.security.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RoleTest {

    @Test
    @DisplayName("role_isTenantScoped_shouldReturnFalse_forAdmin")
    void role_isTenantScoped_shouldReturnFalse_forAdmin() {
        assertFalse(Role.ADMIN.isTenantScoped());
    }

    @Test
    @DisplayName("role_isTenantScoped_shouldReturnTrue_forOwnerTeacherStudent")
    void role_isTenantScoped_shouldReturnTrue_forOwnerTeacherStudent() {
        assertTrue(Role.OWNER.isTenantScoped());
        assertTrue(Role.TEACHER.isTenantScoped());
        assertTrue(Role.STUDENT.isTenantScoped());
    }

    @Test
    @DisplayName("role_asAuthority_shouldPrefixWithRole")
    void role_asAuthority_shouldPrefixWithRole() {
        assertEquals("ROLE_TEACHER", Role.TEACHER.asAuthority());
    }

    @Test
    @DisplayName("role_toAuthorities_shouldMapAllAndReturnImmutableSet")
    void role_toAuthorities_shouldMapAllAndReturnImmutableSet() {
        final Set<String> authorities = Role.toAuthorities(Set.of(Role.ADMIN, Role.OWNER));

        assertEquals(Set.of("ROLE_ADMIN", "ROLE_OWNER"), authorities);
        assertThrows(UnsupportedOperationException.class, () -> authorities.add("ROLE_STUDENT"));
    }

    @Test
    @DisplayName("role_fromRealmRoles_shouldNormalizeUppercaseAndIgnoreUnknown")
    void role_fromRealmRoles_shouldNormalizeUppercaseAndIgnoreUnknown() {
        final Set<Role> roles = Role.fromRealmRoles(Set.of("owner", "TeAcHeR", "foo"));

        assertEquals(Set.of(Role.OWNER, Role.TEACHER), roles);
    }
}
