package com.sotium.identity.domain.model;

import java.time.OffsetDateTime;
import java.util.UUID;

public record UserProfile(
    UUID id,
    UUID userId,
    String firstName,
    String lastName,
    String phone,
    String avatarUrl,
    String bio,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {
}
