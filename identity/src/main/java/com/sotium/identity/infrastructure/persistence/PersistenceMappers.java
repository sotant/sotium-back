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

    public static JpaIdentityUserEntity toEntity(final IdentityUser identityUser) {
        return JpaIdentityUserEntity.fromDomain(
            identityUser.id(),
            identityUser.keycloakSub(),
            identityUser.email(),
            identityUser.status()
        );
    }

    public static JpaMembershipEntity toEntity(final AcademyMembership academyMembership) {
        return JpaMembershipEntity.fromDomain(
            academyMembership.id(),
            academyMembership.academyId(),
            academyMembership.userId(),
            academyMembership.role(),
            academyMembership.status()
        );
    }
}
