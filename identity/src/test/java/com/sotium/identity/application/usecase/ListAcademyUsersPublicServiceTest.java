package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.ListAcademyUsersPublicUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.application.port.out.MembershipRepository;
import com.sotium.identity.application.port.out.UserProfileRepository;
import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import com.sotium.identity.domain.model.MembershipRole;
import com.sotium.identity.domain.model.MembershipStatus;
import com.sotium.identity.domain.model.UserProfile;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ListAcademyUsersPublicServiceTest {

    @Test
    @DisplayName("list_shouldReturnUsersWithProfileAndEmail_whenAcademyHasMemberships")
    void list_shouldReturnUsersWithProfileAndEmail_whenAcademyHasMemberships() {
        final InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        final InMemoryIdentityUserRepository identityUserRepository = new InMemoryIdentityUserRepository();
        final InMemoryUserProfileRepository userProfileRepository = new InMemoryUserProfileRepository();
        final ListAcademyUsersPublicService service = new ListAcademyUsersPublicService(
            membershipRepository,
            userProfileRepository,
            identityUserRepository
        );

        final UUID academyId = UUID.randomUUID();
        final UUID userId = UUID.randomUUID();
        membershipRepository.save(new AcademyMembership(UUID.randomUUID(), academyId, userId, MembershipRole.USER, MembershipStatus.ACTIVE));
        identityUserRepository.save(new IdentityUser(userId, "sub-1", "john.doe@test.com", IdentityUserStatus.ACTIVE));
        userProfileRepository.save(new UserProfile(
            UUID.randomUUID(),
            userId,
            "John",
            "Doe",
            "600123123",
            "https://mock.sotium/avatar/default",
            "Mock bio",
            OffsetDateTime.parse("2026-03-01T10:00:00Z"),
            OffsetDateTime.parse("2026-03-01T10:00:00Z")
        ));

        final ListAcademyUsersPublicUseCase.ListAcademyUsersPublicResult result = service.list(
            new ListAcademyUsersPublicUseCase.ListAcademyUsersPublicCommand(academyId)
        );

        assertEquals(1, result.users().size());
        final ListAcademyUsersPublicUseCase.UserSummary user = result.users().getFirst();
        assertEquals("John", user.firstName());
        assertEquals("Doe", user.lastName());
        assertEquals("john.doe@test.com", user.email());
    }

    private static final class InMemoryIdentityUserRepository implements IdentityUserRepository {

        private final Map<UUID, IdentityUser> users = new HashMap<>();

        @Override
        public Optional<IdentityUser> findById(final UUID identityUserId) {
            return Optional.ofNullable(users.get(identityUserId));
        }

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

    private static final class InMemoryUserProfileRepository implements UserProfileRepository {

        private final Map<UUID, UserProfile> profiles = new HashMap<>();

        @Override
        public Optional<UserProfile> findByUserId(final UUID userId) {
            return profiles.values().stream().filter(profile -> profile.userId().equals(userId)).findFirst();
        }

        @Override
        public UserProfile save(final UserProfile userProfile) {
            profiles.put(userProfile.id(), userProfile);
            return userProfile;
        }
    }

    private static final class InMemoryMembershipRepository implements MembershipRepository {

        private final Map<UUID, AcademyMembership> memberships = new HashMap<>();

        @Override
        public List<AcademyMembership> findActiveMembershipsByUserId(final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId) && membership.isActive())
                .toList();
        }

        @Override
        public List<AcademyMembership> findByUserId(final UUID userId) {
            return memberships.values().stream().filter(membership -> membership.userId().equals(userId)).toList();
        }

        @Override
        public List<AcademyMembership> findByAcademyId(final UUID academyId) {
            return memberships.values().stream().filter(membership -> membership.academyId().equals(academyId)).toList();
        }

        @Override
        public Optional<AcademyMembership> findByAcademyIdAndUserId(final UUID academyId, final UUID userId) {
            return memberships.values().stream()
                .filter(membership -> membership.academyId().equals(academyId) && membership.userId().equals(userId))
                .findFirst();
        }

        @Override
        public AcademyMembership save(final AcademyMembership academyMembership) {
            memberships.put(academyMembership.id(), academyMembership);
            return academyMembership;
        }

        @Override
        public void deleteByUserId(final UUID userId) {
            memberships.entrySet().removeIf(entry -> entry.getValue().userId().equals(userId));
        }
    }
}
