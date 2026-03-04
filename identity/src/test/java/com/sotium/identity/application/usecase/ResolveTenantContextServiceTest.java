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
    @DisplayName("resolveTenantContext_shouldReturnSelectedAcademy_whenSelectedBelongsToActiveMembership")
    void resolveTenantContext_shouldReturnSelectedAcademy_whenSelectedBelongsToActiveMembership() {
        final UUID userId = UUID.randomUUID();
        final UUID selectedAcademyId = UUID.randomUUID();

        final ResolveTenantContextService service = new ResolveTenantContextService(
            activeUserRepository(userId),
            membershipRepository(List.of(
                activeMembership(userId, selectedAcademyId),
                activeMembership(userId, UUID.randomUUID())
            ))
        );

        final UUID resolvedAcademyId = service.resolveAcademyId("sub-1", selectedAcademyId);

        assertEquals(selectedAcademyId, resolvedAcademyId);
    }

    @Test
    @DisplayName("resolveTenantContext_shouldReturnOnlyMembershipAcademy_whenNoSelectionAndSingleMembership")
    void resolveTenantContext_shouldReturnOnlyMembershipAcademy_whenNoSelectionAndSingleMembership() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final ResolveTenantContextService service = new ResolveTenantContextService(
            activeUserRepository(userId),
            membershipRepository(List.of(activeMembership(userId, academyId)))
        );

        final UUID resolvedAcademyId = service.resolveAcademyId("sub-1", null);

        assertEquals(academyId, resolvedAcademyId);
    }

    @Test
    @DisplayName("resolveTenantContext_shouldThrow_whenUserNotProvisioned")
    void resolveTenantContext_shouldThrow_whenUserNotProvisioned() {
        final ResolveTenantContextService service = new ResolveTenantContextService(
            emptyUserRepository(),
            membershipRepository(List.of())
        );

        final IdentityAccessDeniedException exception = assertThrows(
            IdentityAccessDeniedException.class,
            () -> service.resolveAcademyId("missing-sub", null)
        );

        assertEquals("Authenticated user is not provisioned in identity context", exception.getMessage());
    }

    @Test
    @DisplayName("resolveTenantContext_shouldThrow_whenUserStatusNotActive")
    void resolveTenantContext_shouldThrow_whenUserStatusNotActive() {
        final UUID userId = UUID.randomUUID();
        final IdentityUserRepository userRepository = new InMemoryIdentityUserRepository(
            List.of(new IdentityUser(userId, "sub-1", "user@test.com", IdentityUserStatus.INVITED))
        );

        final ResolveTenantContextService service = new ResolveTenantContextService(
            userRepository,
            membershipRepository(List.of(activeMembership(userId, UUID.randomUUID())))
        );

        final IdentityAccessDeniedException exception = assertThrows(
            IdentityAccessDeniedException.class,
            () -> service.resolveAcademyId("sub-1", null)
        );

        assertEquals("Authenticated user is not provisioned in identity context", exception.getMessage());
    }

    @Test
    @DisplayName("resolveTenantContext_shouldThrow_whenNoActiveMemberships")
    void resolveTenantContext_shouldThrow_whenNoActiveMemberships() {
        final UUID userId = UUID.randomUUID();

        final ResolveTenantContextService service = new ResolveTenantContextService(
            activeUserRepository(userId),
            membershipRepository(List.of())
        );

        final IdentityAccessDeniedException exception = assertThrows(
            IdentityAccessDeniedException.class,
            () -> service.resolveAcademyId("sub-1", null)
        );

        assertEquals("User does not have active academy memberships", exception.getMessage());
    }

    @Test
    @DisplayName("resolveTenantContext_shouldThrow_whenSelectedAcademyNotOwnedByUser")
    void resolveTenantContext_shouldThrow_whenSelectedAcademyNotOwnedByUser() {
        final UUID userId = UUID.randomUUID();
        final UUID selectedAcademyId = UUID.randomUUID();

        final ResolveTenantContextService service = new ResolveTenantContextService(
            activeUserRepository(userId),
            membershipRepository(List.of(activeMembership(userId, UUID.randomUUID())))
        );

        final IdentityAccessDeniedException exception = assertThrows(
            IdentityAccessDeniedException.class,
            () -> service.resolveAcademyId("sub-1", selectedAcademyId)
        );

        assertEquals("Selected academy does not belong to the authenticated user", exception.getMessage());
    }

    @Test
    @DisplayName("resolveTenantContext_shouldThrow_whenMultipleActiveMembershipsAndNoSelection")
    void resolveTenantContext_shouldThrow_whenMultipleActiveMembershipsAndNoSelection() {
        final UUID userId = UUID.randomUUID();

        final ResolveTenantContextService service = new ResolveTenantContextService(
            activeUserRepository(userId),
            membershipRepository(List.of(
                activeMembership(userId, UUID.randomUUID()),
                activeMembership(userId, UUID.randomUUID())
            ))
        );

        final IdentityAccessDeniedException exception = assertThrows(
            IdentityAccessDeniedException.class,
            () -> service.resolveAcademyId("sub-1", null)
        );

        assertEquals("Multiple memberships found; explicit academy selection is required", exception.getMessage());
    }

    private IdentityUserRepository activeUserRepository(final UUID userId) {
        return new InMemoryIdentityUserRepository(List.of(
            new IdentityUser(userId, "sub-1", "owner@test.com", IdentityUserStatus.ACTIVE)
        ));
    }

    private IdentityUserRepository emptyUserRepository() {
        return new InMemoryIdentityUserRepository(List.of());
    }

    private MembershipRepository membershipRepository(final List<AcademyMembership> memberships) {
        return new InMemoryMembershipRepository(memberships);
    }

    private AcademyMembership activeMembership(final UUID userId, final UUID academyId) {
        return new AcademyMembership(UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE);
    }

    private static final class InMemoryIdentityUserRepository implements IdentityUserRepository {

        private final List<IdentityUser> users;

        private InMemoryIdentityUserRepository(final List<IdentityUser> users) {
            this.users = users;
        }

        @Override
        public Optional<IdentityUser> findByKeycloakSub(final String keycloakSub) {
            return users.stream().filter(user -> user.keycloakSub().equals(keycloakSub)).findFirst();
        }

        @Override
        public Optional<IdentityUser> findByEmail(final String email) {
            return users.stream().filter(user -> user.email().equals(email)).findFirst();
        }

        @Override
        public IdentityUser save(final IdentityUser identityUser) {
            return identityUser;
        }
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {

        private final List<AcademyMembership> memberships;

        private InMemoryMembershipRepository(final List<AcademyMembership> memberships) {
            this.memberships = memberships;
        }

        @Override
        public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
            return memberships;
        }

        @Override
        public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
            return memberships.stream()
                .filter(membership -> membership.academyId().equals(academyId) && membership.userId().equals(userId))
                .findFirst();
        }

        @Override
        public AcademyMembership save(final AcademyMembership academyMembership) {
            return academyMembership;
        }
    }
}
