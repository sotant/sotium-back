package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityUserEmailConflictException;
import com.sotium.identity.application.port.in.EnsureIdentityUserCommand;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase;
import com.sotium.identity.application.port.in.EnsureIdentityUserResult;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnsureIdentityUserExistsFromTokenService implements EnsureIdentityUserExistsFromTokenUseCase {

    private final IdentityUserRepository identityUserRepository;

    @Override
    public EnsureIdentityUserResult ensure(final EnsureIdentityUserCommand command) {
        Objects.requireNonNull(command, "ensureIdentityUserCommand cannot be null");
        final String keycloakSub = requireNonBlank(command.keycloakSub(), "keycloakSub cannot be blank");
        final String email = requireNonBlank(command.email(), "email cannot be blank").toLowerCase();

        final Optional<IdentityUser> existingUser = identityUserRepository.findByKeycloakSub(keycloakSub);
        if (existingUser.isEmpty()) {
            ensureEmailIsAvailable(email, null);
            final IdentityUser createdUser = identityUserRepository.save(
                new IdentityUser(UUID.randomUUID(), keycloakSub, email, IdentityUserStatus.ACTIVE)
            );
            return new EnsureIdentityUserResult(createdUser.id(), true, false);
        }

        final IdentityUser currentUser = existingUser.orElseThrow();
        if (currentUser.email().equals(email)) {
            return new EnsureIdentityUserResult(currentUser.id(), false, false);
        }

        // We allow email synchronization from the IdP, but only when the destination email is not already bound
        // to another identity user because email remains globally unique inside this bounded context.
        ensureEmailIsAvailable(email, currentUser.id());

        final IdentityUser updatedUser = identityUserRepository.save(
            new IdentityUser(currentUser.id(), currentUser.keycloakSub(), email, currentUser.status())
        );
        return new EnsureIdentityUserResult(updatedUser.id(), false, true);
    }

    private void ensureEmailIsAvailable(final String email, final UUID currentUserId) {
        final Optional<IdentityUser> emailOwner = identityUserRepository.findByEmail(email);
        if (emailOwner.isPresent() && !emailOwner.orElseThrow().id().equals(currentUserId)) {
            throw new IdentityUserEmailConflictException("Email is already assigned to another identity user");
        }
    }

    private String requireNonBlank(final String value, final String message) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(message);
        }
        return value.trim();
    }
}
