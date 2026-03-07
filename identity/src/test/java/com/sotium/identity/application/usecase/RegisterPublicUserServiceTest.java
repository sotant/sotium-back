package com.sotium.identity.application.usecase;

import com.sotium.identity.application.port.in.RegisterPublicUserUseCase;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RegisterPublicUserServiceTest {

    @Test
    @DisplayName("register_shouldCreateIdentityProfileAndMembership_whenRequestIsValid")
    void register_shouldCreateIdentityProfileAndMembership_whenRequestIsValid() {
        final InMemoryIdentityUserRepository identityRepository = new InMemoryIdentityUserRepository();
        final InMemoryUserProfileRepository profileRepository = new InMemoryUserProfileRepository();
        final InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        final RegisterPublicUserService service = new RegisterPublicUserService(identityRepository, profileRepository, membershipRepository);

        final UUID academyId = UUID.randomUUID();
        final RegisterPublicUserUseCase.RegisterPublicUserResult result = service.register(
            new RegisterPublicUserUseCase.RegisterPublicUserCommand(
                academyId,
                "new.user@test.com",
                "John",
                "Doe",
                "600123123"
            )
        );

        assertTrue(result.created());
        final IdentityUser createdIdentity = identityRepository.findByEmail("new.user@test.com").orElseThrow();
        assertEquals(IdentityUserStatus.ACTIVE, createdIdentity.status());

        final UserProfile createdProfile = profileRepository.findByUserId(createdIdentity.id()).orElseThrow();
        assertEquals("John", createdProfile.firstName());
        assertEquals("Doe", createdProfile.lastName());

        final AcademyMembership createdMembership = membershipRepository.findByUserId(createdIdentity.id()).getFirst();
        assertEquals(academyId, createdMembership.academyId());
        assertEquals(MembershipRole.USER, createdMembership.role());
        assertEquals(MembershipStatus.ACTIVE, createdMembership.status());
        assertEquals(createdMembership.id(), result.membershipId());
    }

    @Test
    @DisplayName("register_shouldUseMockValues_whenOptionalFieldsAreBlank")
    void register_shouldUseMockValues_whenOptionalFieldsAreBlank() {
        final InMemoryIdentityUserRepository identityRepository = new InMemoryIdentityUserRepository();
        final InMemoryUserProfileRepository profileRepository = new InMemoryUserProfileRepository();
        final InMemoryMembershipRepository membershipRepository = new InMemoryMembershipRepository();
        final RegisterPublicUserService service = new RegisterPublicUserService(identityRepository, profileRepository, membershipRepository);

        service.register(new RegisterPublicUserUseCase.RegisterPublicUserCommand(
            UUID.randomUUID(),
            "mock.user@test.com",
            "",
            null,
            "  "
        ));

        final IdentityUser createdIdentity = identityRepository.findByEmail("mock.user@test.com").orElseThrow();
        final UserProfile createdProfile = profileRepository.findByUserId(createdIdentity.id()).orElseThrow();
        assertEquals("Mock Name", createdProfile.firstName());
        assertEquals("Mock Surname", createdProfile.lastName());
        assertEquals("000000000", createdProfile.phone());
    }

    @Test
    @DisplayName("register_shouldThrow_whenEmailAlreadyExists")
    void register_shouldThrow_whenEmailAlreadyExists() {
        final InMemoryIdentityUserRepository identityRepository = new InMemoryIdentityUserRepository();
        identityRepository.save(new IdentityUser(UUID.randomUUID(), UUID.randomUUID().toString(), "duplicate@test.com", IdentityUserStatus.ACTIVE));

        final RegisterPublicUserService service = new RegisterPublicUserService(
            identityRepository,
            new InMemoryUserProfileRepository(),
            new InMemoryMembershipRepository()
        );

        assertThrows(
            IllegalArgumentException.class,
            () -> service.register(new RegisterPublicUserUseCase.RegisterPublicUserCommand(
                UUID.randomUUID(),
                "duplicate@test.com",
                "John",
                "Doe",
                "600123123"
            ))
        );
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
            return memberships.values().stream()
                .filter(membership -> membership.userId().equals(userId))
                .toList();
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
