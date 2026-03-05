package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityUserEmailConflictException;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase.EnsureIdentityUserCommand;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase.EnsureIdentityUserResult;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class EnsureIdentityUserExistsFromTokenServiceTest {

    @Test
    @DisplayName("ensure_shouldCreateUser_whenSubDoesNotExist")
    void ensure_shouldCreateUser_whenSubDoesNotExist() {
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository();
        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        final EnsureIdentityUserResult result = service.ensure(new EnsureIdentityUserCommand("sub-1", "owner@test.com"));

        assertTrue(result.created());
        assertFalse(result.emailUpdated());
        assertEquals(result.userId(), repository.findByKeycloakSub("sub-1").orElseThrow().id());
    }

    @Test
    @DisplayName("ensure_shouldUpdateEmail_whenSubExistsAndEmailChanged")
    void ensure_shouldUpdateEmail_whenSubExistsAndEmailChanged() {
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository();
        final IdentityUser user = new IdentityUser(UUID.randomUUID(), "sub-1", "old@test.com", IdentityUserStatus.ACTIVE);
        repository.save(user);

        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        final EnsureIdentityUserResult result = service.ensure(new EnsureIdentityUserCommand("sub-1", "new@test.com"));

        assertFalse(result.created());
        assertTrue(result.emailUpdated());
        assertEquals(user.id(), result.userId());
        assertEquals("new@test.com", repository.findByKeycloakSub("sub-1").orElseThrow().email());
    }

    @Test
    @DisplayName("ensure_shouldThrow_whenEmailBelongsToDifferentUser")
    void ensure_shouldThrow_whenEmailBelongsToDifferentUser() {
        final InMemoryIdentityUserRepository repository = new InMemoryIdentityUserRepository();
        repository.save(new IdentityUser(UUID.randomUUID(), "sub-1", "owner-1@test.com", IdentityUserStatus.ACTIVE));
        repository.save(new IdentityUser(UUID.randomUUID(), "sub-2", "owner-2@test.com", IdentityUserStatus.ACTIVE));

        final EnsureIdentityUserExistsFromTokenService service = new EnsureIdentityUserExistsFromTokenService(repository);

        assertThrows(
            IdentityUserEmailConflictException.class,
            () -> service.ensure(new EnsureIdentityUserCommand("sub-1", "owner-2@test.com"))
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
}
