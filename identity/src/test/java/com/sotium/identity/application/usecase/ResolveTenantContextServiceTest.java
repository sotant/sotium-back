package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityAccessDeniedException;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ResolveTenantContextServiceTest {

    @Test
    @DisplayName("givenValidActiveUserWithSingleMembership_whenNoTenantSelected_thenReturnMembershipAcademy")
    void givenValidActiveUserWithSingleMembership_whenNoTenantSelected_thenReturnMembershipAcademy() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final IdentityUserRepository userRepository = keycloakSub -> Optional.of(new IdentityUser(userId, keycloakSub, "owner@test.com", IdentityUserStatus.ACTIVE));
        final MembershipRepository membershipRepository = ignoredUserId -> List.of(
            new AcademyMembership(UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE)
        );

        final ResolveTenantContextService service = new ResolveTenantContextService(userRepository, membershipRepository);
        final UUID resolved = service.resolveAcademyId("sub-1", null);

        assertEquals(academyId, resolved);
    }

    @Test
    @DisplayName("givenJwtSubjectNotProvisioned_whenResolveTenant_thenThrowIdentityAccessDenied")
    void givenJwtSubjectNotProvisioned_whenResolveTenant_thenThrowIdentityAccessDenied() {
        final IdentityUserRepository userRepository = ignored -> Optional.empty();
        final MembershipRepository membershipRepository = ignored -> List.of();

        final ResolveTenantContextService service = new ResolveTenantContextService(userRepository, membershipRepository);

        assertThrows(IdentityAccessDeniedException.class, () -> service.resolveAcademyId("missing-sub", null));
    }
}
