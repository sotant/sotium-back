package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityUserEmailConflictException;
import com.sotium.identity.application.port.in.EnsureIdentityUserCommand;
import com.sotium.identity.application.port.in.EnsureIdentityUserResult;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnsureIdentityUserExistsFromTokenServiceTest {

    @Test
    @DisplayName("ensure_shouldCreateIdentityUser_whenSubjectDoesNotExist")
    void ensure_shouldCreateIdentityUser_whenSubjectDoesNotExist() {
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository();
        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        final EnsureIdentityUserResult result = service.ensure(new EnsureIdentityUserCommand("sub-1", "owner@test.com"));

        assertTrue(result.created());
        assertFalse(result.emailUpdated());
        assertEquals(1, repository.users.size());
        assertEquals(result.userId(), repository.users.getFirst().id());
    }

    @Test
    @DisplayName("ensure_shouldUpdateEmail_whenSubjectExistsAndEmailChanges")
    void ensure_shouldUpdateEmail_whenSubjectExistsAndEmailChanges() {
        final UUID userId = UUID.randomUUID();
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository(
            new IdentityUser(userId, "sub-1", "old@test.com", IdentityUserStatus.ACTIVE)
        );
        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        final EnsureIdentityUserResult result = service.ensure(new EnsureIdentityUserCommand("sub-1", "new@test.com"));

        assertFalse(result.created());
        assertTrue(result.emailUpdated());
        assertEquals(userId, result.userId());
        assertEquals("new@test.com", repository.users.getFirst().email());
    }

    @Test
    @DisplayName("ensure_shouldThrowConflict_whenEmailIsAssignedToAnotherIdentityUser")
    void ensure_shouldThrowConflict_whenEmailIsAssignedToAnotherIdentityUser() {
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository(
            new IdentityUser(UUID.randomUUID(), "sub-1", "owner-a@test.com", IdentityUserStatus.ACTIVE),
            new IdentityUser(UUID.randomUUID(), "sub-2", "owner-b@test.com", IdentityUserStatus.ACTIVE)
        );
        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        assertThrows(
            IdentityUserEmailConflictException.class,
            () -> service.ensure(new EnsureIdentityUserCommand("sub-1", "owner-b@test.com"))
        );
    }

    private static final class InMemoryIdentityUserRepository implements IdentityUserRepository {

        private final List<IdentityUser> users = new ArrayList<>();

        private InMemoryIdentityUserRepository(final IdentityUser... seedUsers) {
            users.addAll(List.of(seedUsers));
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
            users.removeIf(user -> user.id().equals(identityUser.id()));
            users.add(identityUser);
            return identityUser;
        }
    }
}
