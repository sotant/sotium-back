package com.sotium.identity.application.usecase;

import com.sotium.identity.application.exception.IdentityUserEmailConflictException;
import com.sotium.identity.application.port.in.EnsureIdentityUserExistsFromTokenUseCase;
import com.sotium.identity.application.port.out.IdentityUserRepository;
import com.sotium.identity.domain.model.IdentityUser;
import com.sotium.identity.domain.model.IdentityUserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EnsureIdentityUserExistsFromTokenService implements EnsureIdentityUserExistsFromTokenUseCase {

    private final IdentityUserRepository identityUserRepository;

    @Override
    public EnsureIdentityUserResult ensure(final EnsureIdentityUserCommand command) {
        final String keycloakSub = requireNonBlank(command.keycloakSub(), "keycloakSub");
        final String email = requireNonBlank(command.email(), "email");

        return identityUserRepository.findByKeycloakSub(keycloakSub)
            .map(existingUser -> ensureExistingUser(existingUser, email))
            .orElseGet(() -> createUser(keycloakSub, email));
    }

    private EnsureIdentityUserResult ensureExistingUser(final IdentityUser existingUser, final String email) {
        if (existingUser.email().equals(email)) {
            return new EnsureIdentityUserResult(existingUser.id(), false, false);
        }

        // We allow syncing email from the IdP because it can legitimately change,
        // but we explicitly protect uniqueness across users to preserve traceability.
        identityUserRepository.findByEmail(email)
            .filter(user -> !user.id().equals(existingUser.id()))
            .ifPresent(conflictingUser -> {
                throw new IdentityUserEmailConflictException("Email is already used by another identity user");
            });

        final IdentityUser updatedUser = new IdentityUser(
            existingUser.id(),
            existingUser.keycloakSub(),
            email,
            existingUser.status()
        );
        identityUserRepository.save(updatedUser);

        return new EnsureIdentityUserResult(existingUser.id(), false, true);
    }

    private EnsureIdentityUserResult createUser(final String keycloakSub, final String email) {
        identityUserRepository.findByEmail(email)
            .ifPresent(existingUser -> {
                throw new IdentityUserEmailConflictException("Email is already used by another identity user");
            });

        final IdentityUser createdUser = identityUserRepository.save(
            new IdentityUser(UUID.randomUUID(), keycloakSub, email, IdentityUserStatus.ACTIVE)
        );
        return new EnsureIdentityUserResult(createdUser.id(), true, false);
    }

    private String requireNonBlank(final String value, final String fieldName) {
        Objects.requireNonNull(value, fieldName + " must not be null");
        if (value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return value;
    }
}
