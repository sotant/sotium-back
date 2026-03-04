package com.sotium.identity.application.port.in;

import java.util.UUID;

public interface EnsureIdentityUserExistsFromTokenUseCase {

    EnsureIdentityUserResult ensure(EnsureIdentityUserCommand command);

    record EnsureIdentityUserCommand(String keycloakSub, String email) {
    }

    record EnsureIdentityUserResult(UUID userId, boolean created, boolean emailUpdated) {
    }
}
