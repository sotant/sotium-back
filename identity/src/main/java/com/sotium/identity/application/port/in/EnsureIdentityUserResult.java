package com.sotium.identity.application.port.in;

import java.util.UUID;

public record EnsureIdentityUserResult(
    UUID userId,
    boolean created,
    boolean emailUpdated
) {
}
