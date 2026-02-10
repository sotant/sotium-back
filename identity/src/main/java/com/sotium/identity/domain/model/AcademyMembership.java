package com.sotium.identity.domain.model;

import java.util.UUID;

public record AcademyMembership(
    UUID id,
    UUID academyId,
    UUID userId,
    MembershipRole role,
    MembershipStatus status
) {

    public boolean isActive() {
        return status == MembershipStatus.ACTIVE;
    }
}
