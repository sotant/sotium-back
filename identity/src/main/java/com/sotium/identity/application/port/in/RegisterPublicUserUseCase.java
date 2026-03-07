package com.sotium.identity.application.port.in;

import java.util.UUID;

public interface RegisterPublicUserUseCase {

    RegisterPublicUserResult register(RegisterPublicUserCommand command);

    record RegisterPublicUserCommand(
        UUID academyId,
        String email,
        String name,
        String surname,
        String phone
    ) {
    }

    record RegisterPublicUserResult(UUID userId, UUID membershipId, boolean created) {
    }
}
