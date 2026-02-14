package com.sotium.identity.infrastructure.persistence;

import com.sotium.identity.domain.model.AcademyMembership;
import com.sotium.identity.domain.model.IdentityUser;

public final class PersistenceMappers {

    private PersistenceMappers() {
    }

    public static IdentityUser toDomain(final JpaIdentityUserEntity entity) {
        return new IdentityUser(entity.getId(), entity.getKeycloakSub(), entity.getEmail(), entity.getStatus());
    }

    public static AcademyMembership toDomain(final JpaMembershipEntity entity) {
        return new AcademyMembership(entity.getId(), entity.getAcademyId(), entity.getUserId(), entity.getRole(), entity.getStatus());
    }
}
