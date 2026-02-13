package com.sotium.identity.infrastructure.security;

import com.sotium.identity.application.exception.IdentityAccessDeniedException;
import com.sotium.identity.application.port.in.ResolveTenantContextUseCase;
import com.sotium.shared.security.domain.model.AuthenticatedUser;
import com.sotium.shared.security.domain.model.Role;
import com.sotium.shared.security.infrastructure.security.exceptions.ForbiddenException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TenantAccessPortAdapterTest {

    @Test
    @DisplayName("tenantAccessPortAdapter_shouldDelegateToUseCase")
    void tenantAccessPortAdapter_shouldDelegateToUseCase() {
        final UUID academyId = UUID.randomUUID();
        final UUID selectedAcademyId = UUID.randomUUID();
        final String expectedSub = "sub-1";

        final ResolveTenantContextUseCase useCase = (keycloakSub, selectedId) -> {
            assertEquals(expectedSub, keycloakSub);
            assertEquals(selectedAcademyId, selectedId);
            return academyId;
        };

        final TenantAccessPortAdapter adapter = new TenantAccessPortAdapter(useCase);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser(expectedSub, "owner@test.com", Set.of(Role.OWNER));

        final UUID resolvedAcademyId = adapter.resolveAcademyId(authenticatedUser, selectedAcademyId);

        assertEquals(academyId, resolvedAcademyId);
    }

    @Test
    @DisplayName("tenantAccessPortAdapter_shouldTranslateIdentityAccessDeniedToForbidden")
    void tenantAccessPortAdapter_shouldTranslateIdentityAccessDeniedToForbidden() {
        final String expectedMessage = "Selected academy does not belong to the authenticated user";
        final ResolveTenantContextUseCase useCase = (keycloakSub, selectedId) -> {
            throw new IdentityAccessDeniedException(expectedMessage);
        };

        final TenantAccessPortAdapter adapter = new TenantAccessPortAdapter(useCase);
        final AuthenticatedUser authenticatedUser = new AuthenticatedUser("sub-1", "owner@test.com", Set.of(Role.OWNER));

        final ForbiddenException exception = assertThrows(
            ForbiddenException.class,
            () -> adapter.resolveAcademyId(authenticatedUser, UUID.randomUUID())
        );

        assertEquals(expectedMessage, exception.getMessage());
    }
}
