package com.sotium.identity.application.usecase;

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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResolveIdentityTenantAccessServiceTest {

    @Test
    @DisplayName("hasAccessToAcademy_shouldReturnTrue_whenActiveMembershipExists")
    void hasAccessToAcademy_shouldReturnTrue_whenActiveMembershipExists() {
        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();

        final ResolveIdentityTenantAccessService service = new ResolveIdentityTenantAccessService(
            new StubIdentityUserRepository(new IdentityUser(userId, "sub-1", "owner@test.com", IdentityUserStatus.ACTIVE)),
            new StubMembershipRepository(List.of(
                new AcademyMembership(UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE)
            ))
        );

        assertTrue(service.hasAccessToAcademy("sub-1", academyId));
    }

    @Test
    @DisplayName("hasAccessToAcademy_shouldReturnFalse_whenUserIsInactiveOrUnknown")
    void hasAccessToAcademy_shouldReturnFalse_whenUserIsInactiveOrUnknown() {
        final UUID academyId = UUID.randomUUID();
        final ResolveIdentityTenantAccessService service = new ResolveIdentityTenantAccessService(
            new StubIdentityUserRepository(new IdentityUser(UUID.randomUUID(), "sub-1", "owner@test.com", IdentityUserStatus.DISABLED)),
            new StubMembershipRepository(List.of())
        );

        assertFalse(service.hasAccessToAcademy("sub-1", academyId));
        assertFalse(service.hasAccessToAcademy("missing", academyId));
    }

    private record StubIdentityUserRepository(IdentityUser user) implements IdentityUserRepository {

        @Override
        public Optional<IdentityUser> findByKeycloakSub(final String keycloakSub) {
            return user != null && user.keycloakSub().equals(keycloakSub) ? Optional.of(user) : Optional.empty();
        }

        @Override
        public Optional<IdentityUser> findByEmail(final String email) {
            return Optional.empty();
        }

        @Override
        public IdentityUser save(final IdentityUser identityUser) {
            return identityUser;
        }
    }

    private record StubMembershipRepository(List<AcademyMembership> memberships) implements MembershipRepository {

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
