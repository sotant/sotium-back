package com.sotium.identity.domain.model;

import java.util.UUID;

public record IdentityUser(
    UUID id,
    String keycloakSub,
    String email,
    IdentityUserStatus status
) {
}
