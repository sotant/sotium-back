package com.sotium.identity.application.usecase;

import com.sotium.academy.application.port.out.AcademyRepositoryPort;
import com.sotium.academy.application.port.out.AcademySettingsRepositoryPort;
import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase.DeleteIdentityBySubCommand;
import com.sotium.identity.application.port.in.DeleteIdentityBySubUseCase.DeleteIdentityBySubResult;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DeleteIdentityBySubServiceTest {

    @Test
    @DisplayName("delete_shouldRemoveMembershipAcademyAndIdentity_whenSubExists")
    void delete_shouldRemoveMembershipAcademyAndIdentity_whenSubExists() {
        final InMemoryIdentityUserRepository identityUserRepository = new InMemoryIdentityUserRepository();
        final InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        final InMemoryAcademySettingsRepository academySettingsRepository = new InMemoryAcademySettingsRepository();
        final InMemoryAcademyRepository academyRepository = new InMemoryAcademyRepository();

        final UUID userId = UUID.randomUUID();
        final UUID academyId = UUID.randomUUID();
        identityUserRepository.save(new IdentityUser(userId, "sub-to-delete", "owner@test.com", IdentityUserStatus.ACTIVE));
        membershipRepository.save(new AcademyMembership(UUID.randomUUID(), academyId, userId, MembershipRole.OWNER, MembershipStatus.ACTIVE));

        final DeleteIdentityBySubService service = new DeleteIdentityBySubService(
            identityUserRepository,
            membershipRepository,
            academySettingsRepository,
            academyRepository
        );

        final DeleteIdentityBySubResult result = service.delete(new DeleteIdentityBySubCommand("sub-to-delete"));

        assertTrue(result.deleted());
        assertTrue(identityUserRepository.findByKeycloakSub("sub-to-delete").isEmpty());
        assertTrue(membershipRepository.findByUserId(userId).isEmpty());
        assertTrue(academyRepository.deletedAcademyIds.contains(academyId));
        assertTrue(academySettingsRepository.deletedSettingsAcademyIds.contains(academyId));
    }

    @Test
    @DisplayName("delete_shouldBeNoOp_whenSubDoesNotExist")
    void delete_shouldBeNoOp_whenSubDoesNotExist() {
        final DeleteIdentityBySubService service = new DeleteIdentityBySubService(
            new InMemoryIdentityUserRepository(),
            new InMemoryMembershipRepository(),
            new InMemoryAcademySettingsRepository(),
            new InMemoryAcademyRepository()
        );

        final DeleteIdentityBySubResult result = service.delete(new DeleteIdentityBySubCommand("missing-sub"));

        assertFalse(result.deleted());
    }

    private static final class InMemoryIdentityUserRepository implements IdentityUserRepository {

        private final Map<UUID, IdentityUser> users = new HashMap<>();

        @Override
        public Optional<IdentityUser> findByKeycloakSub(final String keycloakSub) {
            return users.values().stream().filter(user -> user.keycloakSub().equals(keycloakSub)).findFirst();
        }

        @Override
        public Optional<IdentityUser> findByEmail(final String email) {
            return users.values().stream().filter(user -> user.email().equals(email)).findFirst();
        }

        @Override
        public IdentityUser save(final IdentityUser identityUser) {
            users.put(identityUser.id(), identityUser);
            return identityUser;
        }

        @Override
        public void deleteById(final UUID identityUserId) {
            users.remove(identityUserId);
        }
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {

        private final Map<String, AcademyMembership> memberships = new HashMap<>();

        @Override
        public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId))
                .filter(membership -> membership.status() == MembershipStatus.ACTIVE)
                .toList();
        }

        @Override
        public List<AcademyMembership> findByUserId(final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId))
                .toList();
        }

        @Override
        public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
            return Optional.ofNullable(memberships.get(key(academyId, userId)));
        }

        @Override
        public AcademyMembership save(final AcademyMembership academyMembership) {
            memberships.put(key(academyMembership.academyId(), academyMembership.userId()), academyMembership);
            return academyMembership;
        }

        @Override
        public void deleteByUserId(final UUID userId) {
            memberships.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
        }

        private String key(final UUID academyId, final UUID userId) {
            return academyId + ":" + userId;
        }
    }

    private static final class InMemoryAcademySettingsRepository implements AcademySettingsRepositoryPort {

        private final List<UUID> deletedSettingsAcademyIds = new ArrayList<>();

        @Override
        public com.sotium.academy.domain.model.AcademySettings save(final com.sotium.academy.domain.model.AcademySettings academySettings) {
            return academySettings;
        }

        @Override
        public void deleteByAcademyId(final UUID academyId) {
            deletedSettingsAcademyIds.add(academyId);
        }
    }

    private static final class InMemoryAcademyRepository implements AcademyRepositoryPort {

        private final Set<UUID> deletedAcademyIds = new java.util.HashSet<>();

        @Override
        public com.sotium.academy.domain.model.Academy save(final com.sotium.academy.domain.model.Academy academy) {
            return academy;
        }

        @Override
        public void deleteById(final UUID academyId) {
            deletedAcademyIds.add(academyId);
        }
    }
}
